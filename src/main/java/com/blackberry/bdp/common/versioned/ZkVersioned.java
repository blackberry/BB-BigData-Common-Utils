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
import com.blackberry.bdp.common.exception.DeleteException;
import com.blackberry.bdp.common.exception.InvalidUserRoleException;
import com.blackberry.bdp.common.exception.JsonMergeException;
import com.blackberry.bdp.common.exception.MissingConfigurationException;
import com.blackberry.bdp.common.exception.VersionMismatchException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.lang.reflect.Method;

@JsonIgnoreProperties({"curator", "zkPath", "mode", "retries", "backoff", "backoffExponent"})
public abstract class ZkVersioned {

	private static final Logger LOG = LoggerFactory.getLogger(ZkVersioned.class);
	protected static ObjectMapper mapper;
	private CuratorFramework curator;
	private String zkPath;
	private final Map<String, Map<Class, Class>> roleToMixInMapping = new HashMap<>();
	protected Integer version = null;
	private CreateMode mode = CreateMode.PERSISTENT;
	private long backoff = 1000;
	private long retries = 3;
	private long backoffExponent = 1;

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
			if (!myField.isAnnotationPresent(JsonIgnore.class)) {
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
		curator.delete().deletingChildrenIfNeeded().forPath(zkPath);
	}

	public synchronized static void delete(CuratorFramework curator, String zkPath) throws DeleteException, Exception {
		Stat stat = curator.checkExists().forPath(zkPath);
		if (stat == null) {
			LOG.error("Cannot delete object at non-existent path: {}", zkPath);
			throw new DeleteException(String.format("Cannot delete object at non-existent path: %s", zkPath));
		}
		LOG.info("deleting object at path {}", zkPath);
		curator.delete().deletingChildrenIfNeeded().forPath(zkPath);
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

		for (int i = 0; i < retries; i++) {
			try {
				if (stat == null) {
					if (version != null) {
						throw new VersionMismatchException("New objects must have null version");
					}
					LOG.info("Saving initial object in non-existent zkPath: {}", zkPath);
					curator.create().creatingParentsIfNeeded()
						 .withMode(mode).forPath(zkPath, jsonString.getBytes());
					stat = this.curator.checkExists().forPath(zkPath);
					this.setVersion(stat.getVersion());
				} else {
					if (version == null) {
						throw new VersionMismatchException("Cannot update existing objects with null version");
					}
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
				break;
			} catch (VersionMismatchException vme) {
				throw vme;
			} catch (Exception e) {
				if (i <= retries) {
					LOG.warn("Failed attempt {}/{} to write to {}.  Retrying in {} seconds", i, retries, zkPath, (backoff / 1000), e);
					Thread.sleep(backoff);
					backoff *= backoffExponent;
				} else {
					throw new Exception(String.format("Failed to write to %s and no retries left--giving up", zkPath), e);
				}
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
			jsonToWriteToZk = merge(existingJsonNode, roleBasedJsonNode).toString();
			LOG.info("saving existing object with role {} yields JSON {}", role, jsonToWriteToZk);
		} catch (MissingConfigurationException mce) {
			jsonToWriteToZk = roleBasedJsonNode.toString();
			LOG.info("saving with role {} yields JSON {}", role, jsonToWriteToZk);
		}
		writeJsonToZooKeeper(jsonToWriteToZk);
	}

	/**
	 * Iterates over json1 which is intended to be a full representation of a complete JSON 
	 * structure.  It compares nodes on json1 against nodes on json2 which should contain 
	 * either the same identical structure of json1 or a subset of JSON structure contained 
	 * in json1.
	 * 
	 * If identically named nodes on json1 and json2 vary in type (ObjectNode vs ArrayNode
	 * for example) then an exception is thrown since json2 must not contain any additional 
	 * structure than what is found in json1.
	 * 
	 * Explicit Null Node Handling Regardless of Node type:
	 * 
	 * This pertains to the value of a node being explicity equal to null.  See further below 
	 * for handling of non-existent nodes
	 * 
	 * If a node is null on json1 and not null on json2 then the node on json1 is set to the 
	 * value of the node on json2.
	 * 
	 * If a node is not null on json1 and is null on json2 then the node on json1 is made null.
	 * 
	 * Non-existent Node Handling:
	 *
	 * Since json1 is intended to be a full representation of a  complete JSON structure 
	 * nodes on json2 that don't exist on json1 are completely ignored.  Only if the same
	 * node exists on both json1 and json2 will the structures be merged.
	 * 
	 * ArrayNode Handling
	 * 
	 * If the node being compared is an ArrayNode then the elements on json2 are iterated
	 * over.  If the index on json1 exists on json1 then the two elements are merged.  If the 
	 * index doesn't exist on json1 then the element is added to the ArrayNode on json1.
	 * Note: The existence of the element on json1 is determined by index and when an 
	 * element is added to json1 it's index increases by one.  That shouldn't be a problem 
	 * though as for there to ever be more elements in json2, the index pointer will always 
	 * be one larger than the max index of json1.
	 * 
	 * ArrayNode Handling when json1 contains more elements than json2:
	 * 
	 * If 
	 *
	 * @param json1
	 * @param json2
	 * @return
	 * @throws com.blackberry.bdp.common.exception.JsonMergeException
	 */
	public static JsonNode merge(JsonNode json1, JsonNode json2) throws JsonMergeException {
		Iterator<String> json1FieldNames = json1.fieldNames();
		while (json1FieldNames.hasNext()) {
			String json1Field = json1FieldNames.next();
			JsonNode json1Node = json1.get(json1Field);
			if (json1.getNodeType().equals(json2.getNodeType()) == false) {
				throw new JsonMergeException(String.format("json1 (%s) cannot be merged with json2 (%s)",
					 json1.getNodeType(), json2.getNodeType()));
			} else if (!json2.has(json1Field)) {
				LOG.info("Not comparing {} since it doesn't exist on json2", json1Field);
			} else if (json1Node.isNull() && json2.hasNonNull(json1Field)) {
				((ObjectNode) json1).replace(json1Field, json2.get(json1Field));
				LOG.info("explicitly null node {} on json1 replaced with non-null node on json2", json1Field);
			} else if (json1.hasNonNull(json1Field) && json2.get(json1Field).isNull()) {
				((ObjectNode) json1).replace(json1Field, json2.get(json1Field));
				LOG.info("non-null node {} on json1 replaced with explicitly null node on json2", json1Field);
			} else if (json1.isObject()) {
				LOG.info("Calling merge on Object {}", json1Field);
				merge(json1Node, json2.get(json1Field));
			} else if (json1 instanceof ObjectNode) {
				LOG.info("json1 is an instanceof ObjectNode");				;
				if (json2.get(json1Field) != null) {
					((ObjectNode) json1).replace(json1Field, json2.get(json1Field));
					LOG.info("json1 replaced {} with json2's field", json1Field);
				}
			} else if (json1.isArray()) {
				ArrayNode json1ArrayNode = (ArrayNode) json1.get(json1Field);
				ArrayNode json2ArrayNode = (ArrayNode) json2.get(json1Field);
				LOG.info("ArrayNode {} json1 has {} elements and json2 has {} elements",
					 json1Field, json1ArrayNode.size(), json2ArrayNode.size());
				int indexNo = 0;
				Iterator<JsonNode> json2Iter = json2ArrayNode.iterator();
				while (json2Iter.hasNext()) {
					JsonNode json2Element = json2Iter.next();
					if (!json1Node.has(indexNo)) {
						LOG.info("Need to merge ArrayNode {} element {}", json1Field, indexNo);
						merge(json1Node.get(indexNo), json2Element);
					} else {
						LOG.info("ArrayNode {} element {} not found on json1, adding", json1Field, indexNo);
						json1ArrayNode.add(json2Element);
					}
					indexNo++;
				}
				while (json1ArrayNode.size() > json2ArrayNode.size()) {
					int indexToRemove = json1ArrayNode.size() - 1;
					json1ArrayNode.remove(indexToRemove);
					LOG.info("ArrayNode {} index {} on json1 removed since greater than size of json2 ({})",
						 json1Field, indexToRemove, json2ArrayNode.size());
				}				
			} else {				
				throw new JsonMergeException(String.format("Cannot merge %s unknown type of %s", 
					 json1Field, json1Node.getNodeType()));
			}
		}
		return json1;
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
		obj.setCurator(curator);
		obj.setZkPath(zkPath);
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
	 This is gross and what happens when you try to bend something to your will that
	 should never have been bent that way at all.  Leaving it in as a reminder of hell
	 @param <K>
	 @param <T>
	 @param type
	 @param keyMethod
	 @param keyMethodName
	 @param curator
	 @param zkPathRoot
	 @return
	 @throws Exception 
	 */
	public static <K, T extends ZkVersioned> HashedList<K, T> getAllHashedList(
		 Class<T> type,
		 Class<K> keyMethod,
		 String keyMethodName,
		 CuratorFramework curator,
		 String zkPathRoot) throws Exception {

		Method method = type.getDeclaredMethod(keyMethodName, keyMethod);

		HashedList<K, T> hashedList = new HashedList<>();

		mapper = getNewMapper();
		Stat stat = curator.checkExists().forPath(zkPathRoot);
		if (stat == null) {
			throw new MissingConfigurationException("Configuration doesn't exist in ZK at " + zkPathRoot);
		}

		for (String objectId : Util.childrenInZkPath(curator, zkPathRoot)) {
			String objPath = String.format("%s/%s", zkPathRoot, objectId);
			Stat objStat = curator.checkExists().forPath(objPath);
			byte[] jsonBytes = curator.getData().forPath(objPath);
			if (jsonBytes.length != 0) {
				T obj = mapper.readValue(jsonBytes, type);
				obj.setVersion(objStat.getVersion());
				obj.setCurator(curator);
				obj.setZkPath(objPath);
				hashedList.add((K) method.invoke(obj), obj);
			} else {
				LOG.error("The byte array in {} was empty", objPath);
			}
		}
		return hashedList;
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

	/**
	 * @return the zkPath
	 */
	public String getZkPath() {
		return zkPath;
	}

	/**
	 * @return the mode
	 */
	public CreateMode getMode() {
		return mode;
	}

	/**
	 * @param mode the mode to set
	 */
	public void setMode(CreateMode mode) {
		this.mode = mode;
	}

	/**
	 * @return the backoff
	 */
	public long getBackoff() {
		return backoff;
	}

	/**
	 * @param backoff the backoff to set
	 */
	public void setBackoff(long backoff) {
		this.backoff = backoff;
	}

	/**
	 * @return the retries
	 */
	public long getRetries() {
		return retries;
	}

	/**
	 * @param retries the retries to set
	 */
	public void setRetries(long retries) {
		this.retries = retries;
	}

	/**
	 * @return the backoffExponent
	 */
	public long getBackoffExponent() {
		return backoffExponent;
	}

	/**
	 * @param backoffExponent the backoffExponent to set
	 */
	public void setBackoffExponent(long backoffExponent) {
		this.backoffExponent = backoffExponent;
	}

}
