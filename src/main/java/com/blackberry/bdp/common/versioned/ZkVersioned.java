/*
 * Copyright 2015 BlackBerry Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.blackberry.bdp.common.versioned;

//import com.fasterxml.jackson.annotation.JsonIgnore;
//import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackberry.bdp.common.exception.ComparableClassMismatchException;
import com.blackberry.bdp.common.exception.InvalidUserRoleException;
import com.blackberry.bdp.common.exception.MissingConfigurationException;
import com.blackberry.bdp.common.exception.VersionMismatchException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;

@JsonIgnoreProperties({"curator", "zkPath"})
public abstract class ZkVersioned {

	private static final Logger LOG = LoggerFactory.getLogger(ZkVersioned.class);
	protected static ObjectMapper mapper;
	private CuratorFramework curator;
	private String zkPath;
	private final Map<String, Map<Class, Class>> roleToMixInMapping = new HashMap<>();
	protected Integer version = null;

	public ZkVersioned() {
		mapper = getNewMapper();
	}

	public ZkVersioned(CuratorFramework curator, String zkPath) {
		this();
		this.curator = curator;
		this.zkPath = zkPath;
	}

	private static ObjectMapper getNewMapper() {
		ObjectMapper newMapper = new ObjectMapper();
		newMapper.configure(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS, true);
		return newMapper;
	}

	public void registerMixIn(String role, Class objClass, Class mixinClass) {
		Map<Class, Class> roleToClass = roleToMixInMapping.get(role);
		if (roleToClass == null) {
			roleToClass = new HashMap<>();
			roleToMixInMapping.put(role, roleToClass);
		}
		roleToClass.put(objClass, mixinClass);
		LOG.info("here's the role to mix-in map: {}", roleToMixInMapping);
	}

	public final void reload(ZkVersioned newVersion)
		 throws IllegalArgumentException, IllegalAccessException, ComparableClassMismatchException {

		if (!this.getClass().equals(newVersion.getClass())) {
			throw new ComparableClassMismatchException(
				 String.format("Versioned class %s cannot be compared to %s",
					  this.getClass(), newVersion.getClass()));
		}

		if (this.getVersion() >= newVersion.getVersion()) {
			return;
		}

		for (Field myField : this.getClass().getDeclaredFields()) {
			if (myField.isAnnotationPresent(JsonProperty.class)) {
				JsonProperty anno = myField.getAnnotation(JsonProperty.class);
				if (!myField.get(this).equals(myField.get(newVersion))) {
					// Field mis-match, inherit the new version's value						
					LOG.info("Assigning {}.{}={} (old version: {}, old value: {}, new version {}",
						 this.getClass().getName(),
						 myField.getName(),
						 myField.get(newVersion),
						 this.getVersion(),
						 myField.get(this),
						 newVersion.getVersion());
					myField.set(this, myField.get(newVersion));
				}
			}
		}
	}

	/**
	 * Fetches the new object from ZK
	 *
	 * @throws Exception
	 */
	public final void reload() throws Exception {
		Stat newZkStat = curator.checkExists().forPath(zkPath);
		if (newZkStat == null) {
			throw new MissingConfigurationException("Configuration doesn't exist in ZK at " + zkPath);
		}
		ZkVersioned newObj = mapper.readValue(curator.getData().forPath(zkPath), this.getClass());
		newObj.setVersion(newZkStat.getVersion());
		reload(newObj);
		this.version = newZkStat.getVersion();
	}

	/**
	 * Deletes an object from ZK
	 *
	 * @throws DeleteException
	 * @throws Exception
	 */
	public synchronized void delete() throws DeleteException, Exception {
		Stat stat = this.curator.checkExists().forPath(zkPath);
		if (stat == null) {
			LOG.error("Cannot delete {} object at non-existent path: {}", this, zkPath);
			throw new DeleteException(String.format("Cannot delete object at non-existent path: %s", zkPath));
		}
		curator.delete().forPath(zkPath);
	}

	public synchronized static void delete(CuratorFramework curator, String zkPath) throws DeleteException, Exception {
		Stat stat = curator.checkExists().forPath(zkPath);
		if (stat == null) {
			LOG.error("Cannot delete object at non-existent path: {}", zkPath);
			throw new DeleteException(String.format("Cannot delete object at non-existent path: %s", zkPath));
		}
		LOG.info("deleting object at path {}", zkPath);
		curator.delete().forPath(zkPath);
	}

	public String toJSON() throws JsonProcessingException {
		return mapper.writeValueAsString(this);
	}

	public String toJSON(String role)
		 throws JsonProcessingException, InvalidUserRoleException {
		if (!roleToMixInMapping.containsKey(role)) {
			throw new InvalidUserRoleException(String.format(
				 "The role %s does not apply to %s", role, getClass()));
		}
		ObjectMapper mixinMapper = getNewMapper();
		for (Class objClass : roleToMixInMapping.get(role).keySet()) {
			mixinMapper.addMixIn(objClass, roleToMixInMapping.get(role).get(objClass));
		}
		return mixinMapper.writeValueAsString(this);
	}

	public JsonNode toJsonNode() throws IOException {
		return mapper.readTree(toJSON());
	}

	public JsonNode toJsonNode(String role)
		 throws IOException, JsonProcessingException, InvalidUserRoleException {
		return mapper.readTree(toJSON(role));
	}

	private void writeJsonToZooKeeper(String jsonString) throws Exception {
		// remove the version as that never gets written to ZK
		ObjectNode node = (ObjectNode) mapper.readTree(jsonString);
		node.remove("version");
		jsonString = node.toString();
		LOG.info("Attempt at saving {} to {} as {}", this, this.zkPath, jsonString);
		Stat stat = this.curator.checkExists().forPath(zkPath);
		if (stat == null) {
			if (version != null) throw new VersionMismatchException("New objects must have null version");
			LOG.info("Saving initial object in non-existent zkPath: {}", zkPath);
			curator.create().creatingParentsIfNeeded()
				 .withMode(CreateMode.PERSISTENT).forPath(zkPath, jsonString.getBytes());
			stat = this.curator.checkExists().forPath(zkPath);
			this.setVersion(stat.getVersion());
		} else {
			if (version == null) throw new VersionMismatchException("Cannot update existing objects with null version");
			if (this.version != stat.getVersion()) {
				throw new VersionMismatchException(String.format(
					 "Object with version %s cannot be saved to existing version %s",
					 this.version, stat.getVersion()));
			} else {
				Stat newStat = curator.setData().forPath(zkPath, jsonString.getBytes());
				this.setVersion(newStat.getVersion());
				LOG.info("Saved new {} version {}", this.getClass(), newStat.getVersion());
			}
		}
	}

	public synchronized void save() throws JsonProcessingException,
		 VersionMismatchException, Exception {
		writeJsonToZooKeeper(toJSON());
	}

	public synchronized void save(String role) throws Exception {
		JsonNode roleBasedJsonNode = toJsonNode(role);
		LOG.info("Role based json node : {}", roleBasedJsonNode);
		String jsonToWriteToZk;
		try {
			JsonNode existingJsonNode = get(this.getClass(), this.curator, this.zkPath).toJsonNode();
			LOG.info("existing json node from zk : {}", existingJsonNode);
			jsonToWriteToZk = mergeJSON(existingJsonNode, roleBasedJsonNode).toString();
		} catch (MissingConfigurationException mce) {
			jsonToWriteToZk = roleBasedJsonNode.toString();
		}
		writeJsonToZooKeeper(jsonToWriteToZk);
	}

	public static JsonNode mergeJSON(JsonNode mainNode, JsonNode updateNode) {
		Iterator<String> fieldNames = mainNode.fieldNames();
		while (fieldNames.hasNext()) {
			String fieldName = fieldNames.next();
			JsonNode jsonNode = mainNode.get(fieldName);

			//if field exist and  json node is an array
			if (jsonNode != null && jsonNode.isArray()) {
				JsonNode tempArrayNode = updateNode.get(fieldName);
				int count = 0;
				//iterating array node
				for (JsonNode node : jsonNode) {
					mergeJSON(node, tempArrayNode.get(count));
					count++;
				}
			} else {
				// if field exists and is an embedded object
				if (jsonNode != null && jsonNode.isObject()) {
					mergeJSON(jsonNode, updateNode.get(fieldName));
				} else {
					if (mainNode instanceof ObjectNode) {
						// Overwrite field
						JsonNode value = updateNode.get(fieldName);
						if (value != null) {
							((ObjectNode) mainNode).replace(fieldName, value);
						}
					}
				}
			}
		}
		return mainNode;
	}

	/**
	 * Returns a VersionedObject from a specific CuratorFramework and ZK Path
	 *
	 * @param <T>
	 * @param type
	 * @param curator
	 * @param zkPath
	 * @return
	 * @throws Exception
	 */
	public static <T extends ZkVersioned> T get(
		 Class<T> type,
		 CuratorFramework curator,
		 String zkPath) throws Exception {
		mapper = getNewMapper();
		Stat stat = curator.checkExists().forPath(zkPath);
		if (stat == null) {
			throw new MissingConfigurationException("Configuration doesn't exist in ZK at " + zkPath);
		}
		byte[] jsonBytes = curator.getData().forPath(zkPath);
		T obj = mapper.readValue(jsonBytes, type);
		obj.setVersion(stat.getVersion());
		return obj;
	}

	/**
	 * Returns all VersionedObjects from a specific CuratorFramework and ZK Root Path
	 *
	 * @param <T>
	 * @param type
	 * @param curator
	 * @param zkPathRoot
	 * @return
	 * @throws Exception
	 */
	public static <T extends ZkVersioned> ArrayList<T> getAll(
		 Class<T> type,
		 CuratorFramework curator,
		 String zkPathRoot) throws Exception {
		mapper = getNewMapper();
		Stat stat = curator.checkExists().forPath(zkPathRoot);
		if (stat == null) {
			throw new MissingConfigurationException("Configuration doesn't exist in ZK at " + zkPathRoot);
		}
		ArrayList<T> objList = new ArrayList<>();

		for (String objectId : Util.childrenInZkPath(curator, zkPathRoot)) {
			String objPath = String.format("%s/%s", zkPathRoot, objectId);
			Stat objStat = curator.checkExists().forPath(objPath);
			byte[] jsonBytes = curator.getData().forPath(objPath);
			if (jsonBytes.length != 0) {
				T obj = mapper.readValue(jsonBytes, type);
				obj.setVersion(objStat.getVersion());
				obj.setCurator(curator);
				obj.setZkPath(objPath);
				objList.add(obj);
			} else {
				LOG.error("The byte array in {} was empty", objPath);
			}
		}
		return objList;
	}

	/**
	 * @return the version
	 */
	public Integer getVersion() {
		return version;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(Integer version) {
		this.version = version;
	}

	/**
	 * @param curator the curator to set
	 */
	public void setCurator(CuratorFramework curator) {
		this.curator = curator;
	}

	/**
	 * @param zkPath the zkPath to set
	 */
	public void setZkPath(String zkPath) {
		this.zkPath = zkPath;
	}

}
