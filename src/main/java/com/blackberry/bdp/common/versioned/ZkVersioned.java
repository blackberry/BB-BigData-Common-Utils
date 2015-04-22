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

import java.lang.reflect.Field;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.data.Stat;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ZkVersioned {

	private static final Logger LOG = LoggerFactory.getLogger(ZkVersioned.class);	
	protected final static ObjectMapper mapper = new ObjectMapper();	
	private CuratorFramework curator;
	private String zkPath;
	
	@VersionedAttribute
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
						myField.set(this, myField.get(newVersion));
						LOG.info("Assigning {}.{}={} (old version: {}, old value: {}, new version {}", 
							 this.getClass().getName(),
							 myField.getName(),
							 myField.get(newVersion),
							 this.getVersion(),
							 myField.get(this),
							 newVersion.getVersion());
					}
				}
			}
		}
	}
	
	/**
	 * Fetches the new configuration from ZK
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
	}
	
	protected static ZkVersioned get(CuratorFramework curator, String zkPath) throws Exception {
		Stat stat = curator.checkExists().forPath(zkPath);		
		if (stat == null) {
			throw new MissingConfigurationException("Configuration doesn't exist in ZK at " + zkPath);
		}
		ZkVersioned newObj = mapper.readValue(curator.getData().forPath(zkPath), ZkVersioned.class);
		newObj.setVersion(stat.getVersion());
		return newObj;		
				
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
}