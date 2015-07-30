/*
 * Copyright 2015 dariens.
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
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import java.lang.reflect.Field;
import java.util.ArrayList;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonIgnoreProperties({"version", "curator", "zkPath"})
public abstract class ZkVersioned {

	private static final Logger LOG = LoggerFactory.getLogger(ZkVersioned.class);
	protected final static ObjectMapper mapper = new ObjectMapper();
	private CuratorFramework curator;
	private String zkPath;

	protected Integer version = 0;

	public ZkVersioned() {
	}

	public ZkVersioned(CuratorFramework curator, String zkPath) {
		this.curator = curator;
		this.zkPath = zkPath;
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
		curator.delete().forPath(zkPath);
	}

	public synchronized void save() throws JsonProcessingException, VersionMismatchException, Exception {
		mapper.configure(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS, true);
		mapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
		String jsonObj = mapper.writeValueAsString(this);
		LOG.info("Attempt at saving {} to {} as {}", this, this.zkPath, jsonObj);
		Stat stat = this.curator.checkExists().forPath(zkPath);
		if (stat == null) {
			LOG.info("Saving initial object in non-existent zkPath: {}", zkPath);
			curator.create().creatingParentsIfNeeded()
				 .withMode(CreateMode.PERSISTENT).forPath(zkPath, jsonObj.getBytes());
		} else {
			if (this.version != null && this.version != stat.getVersion()) {
				throw new VersionMismatchException(String.format(
					 "Object with version %s cannot be saved to existing version %s",
					 this.version, stat.getVersion()));
			} else {
				Stat newStat = curator.setData().forPath(zkPath, jsonObj.getBytes());
				this.setVersion(newStat.getVersion());
				LOG.info("Saved new {} version {}", this.getClass(), newStat.getVersion());
			}
		}
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
	public int getVersion() {
		return version;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(int version) {
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
