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

import java.util.List;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZkVersionedTest {

	private static final Logger LOG = LoggerFactory.getLogger(ZkVersionedTest.class);
	private static CuratorFramework curator;
	private static LocalZkServer zk;

	private static CuratorFramework buildCuratorFramework() {
		String connectionString = "localhost:21818";
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
		LOG.info("attempting to connect to ZK with connection string {}", "localhost:21818");
		CuratorFramework newCurator = CuratorFrameworkFactory.newClient(connectionString, retryPolicy);
		return newCurator;
	}

	@BeforeClass
	public static void setup() throws Exception {
		zk = new LocalZkServer();
		curator = buildCuratorFramework();
		curator.start();
	}

	@Test
	public void testSaveNewObject() throws VersionMismatchException, Exception {
		TestObject1 testObject1 = new TestObject1();
		
		testObject1.setLongObject1(Long.MIN_VALUE);
		testObject1.setStringObject1("String 1");
		testObject1.setBooleanObject1(Boolean.FALSE);
		testObject1.setByteObject1(Byte.MIN_VALUE);
		testObject1.setCharObject1(Character.MIN_VALUE);
		testObject1.setDoubleObject1(Double.MIN_NORMAL);
		testObject1.setIntegerObject1(Integer.MIN_VALUE);
		testObject1.setShortObject1(Short.MIN_VALUE);
		
		TestObject2 testObject2a = new TestObject2();
		TestObject2 testObject2b = new TestObject2();
		
		TestObject2[] testObject2Array = {testObject2a, testObject2b};
		testObject1.setTestObject2List(testObject2Array);
		
		testObject1.setCurator(curator);
		testObject1.setZkPath("/testObject1");
		testObject1.save();
	}

	@AfterClass
	public static void cleanup() throws Exception {
		curator.close();
		zk.shutdown();
	}

}

