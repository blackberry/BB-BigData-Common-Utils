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
package com.blackberry.bdp.common.zk;

import com.blackberry.bdp.common.conversion.Converter;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dariens
 */
public class ZkUtils {

	private static final Logger LOG = LoggerFactory.getLogger(ZkUtils.class);
	
	public static void writeToPath(CuratorFramework curator, String path, int i, boolean createIfMissing, CreateMode mode) 
		 throws Exception {
		if (curator.checkExists().forPath(path) == null) {
			curator.create().creatingParentsIfNeeded()
				 .withMode(mode).forPath(path, Converter.getBytes(i));			
		} else {
			curator.setData().forPath(path, Converter.getBytes(i));			
		}
	}
}
