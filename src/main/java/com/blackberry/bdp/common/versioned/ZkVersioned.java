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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Field;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ZkVersioned {

	private static final Logger LOG = LoggerFactory.getLogger(ZkVersioned.class);
	protected final static ObjectMapper mapper = new ObjectMapper();
	private CuratorFramework curator;
	private String zkPath;

	protected int version = 0;

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
			if (myField.isAnnotationPresent(VersionedAttribute.class)) {
				VersionedAttribute anno = myField.getAnnotation(VersionedAttribute.class);
				if (anno.enabled()) {
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
	}

	/**
	 * Fetches the new configuration from ZK
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

	public synchronized void save() throws Exception {
		//mapper.setAnnotationIntrospector(new IgnoreNonVersionedIntrospector());
		//mapper.configure(Feature.FAIL_ON_EMPTY_BEANS, false);				
		/*mapper.setVisibilityChecker(mapper.getSerializationConfig().getDefaultVisibilityChecker()
			 .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
			 .withGetterVisibility(JsonAutoDetect.Visibility.ANY)
			 .withSetterVisibility(JsonAutoDetect.Visibility.ANY)
			 .withCreatorVisibility(JsonAutoDetect.Visibility.ANY));*/
		
		String jsonObj = mapper.writeValueAsString(this);
		LOG.info("Attempt at saving {} as {}", this, jsonObj);
		
		Stat stat = this.curator.checkExists().forPath(zkPath);
		if (stat == null) {
			LOG.info("Saving initial object in non-existent zkPath: {}", zkPath);
			curator.create().creatingParentsIfNeeded()
				 .withMode(CreateMode.PERSISTENT).forPath(zkPath, jsonObj.getBytes());
		} else {
			if (this.version != stat.getVersion()) {
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
