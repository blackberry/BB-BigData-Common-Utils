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

import com.blackberry.bdp.common.exception.InvalidUserRoleException;
import com.blackberry.bdp.common.exception.VersionMismatchException;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import static org.junit.Assert.*;

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
		newCurator.start();
		return newCurator;
	}

	@BeforeClass
	public static void setup() throws Exception {
		zk = new LocalZkServer();
		curator = buildCuratorFramework();		
	}

	private TestObject getTestObject() {
		TestObject testObject = new TestObject(curator, "/testObject");

		testObject.setLongObject(Long.MIN_VALUE);
		testObject.setStringObject("String");

		Pojo pojo1 = new Pojo();
		Pojo pojo2 = new Pojo();

		Pojo[] pojos = {pojo1, pojo2};
		testObject.setPojoList(pojos);
		return testObject;
	}

	@Test(expected = VersionMismatchException.class)
	public void testSaveNewNonNullVersion() throws VersionMismatchException, Exception {
		TestObject testObject = getTestObject();
		testObject.setVersion(1);
		testObject.save();
	}

	@Test(expected = VersionMismatchException.class)
	public void testSaveExistingWithNullVersion() throws VersionMismatchException, Exception {
		TestObject testObject = new TestObject(curator, "/testObjectUniquePath");
		testObject.save();
		testObject.setVersion(null);
		testObject.save();
	}

	@Test
	public void testSaveFetchNewObject() throws VersionMismatchException, Exception {
		TestObject testObject = getTestObject();		
		testObject.save();
		TestObject retrievedObject = TestObject.get(TestObject.class, curator, "/testObject");
		assertEquals(testObject.toJSON(), retrievedObject.toJSON());
	}

	@Test(expected = InvalidUserRoleException.class)
	public void testSavingNonExistentRole() throws Exception {
		TestObject testObject = getTestObject();
		testObject.save("no-role");
	}

	@Test
	public void testApplyRole() throws IOException, JsonProcessingException, InvalidUserRoleException {
		TestObject testObject = getTestObject();
		testObject.setSensitive1("This text should not be serialized if protected by 1");
		testObject.setSensitive2("This text should not be serialized if protected by 2");

		testObject.registerMixIn("protectedBy1", TestObject.class, TestObjectProtected1.class);
		testObject.registerMixIn("protectedBy2", TestObject.class, TestObjectProtected2.class);

		// No role: Both sensitive fields should exist
		assertEquals(testObject.toJsonNode().has("sensitive1"), true);
		assertEquals(testObject.toJsonNode().has("sensitive1"), true);

		// protectedBy1 role: sensitive1 should not exist, sensitive 2 should exist
		assertEquals(testObject.toJsonNode("protectedBy1").has("sensitive1"), false);
		assertEquals(testObject.toJsonNode("protectedBy1").has("sensitive2"), true);

		// Role: protectedBy2, neither sensitive fields should exist
		assertEquals(testObject.toJsonNode("protectedBy2").has("sensitive1"), false); 
		assertEquals(testObject.toJsonNode("protectedBy2").has("sensitive2"), false);

		assertEquals(testObject.toJsonNode().get("pojoList").get(0).has("protectedString"), true);

		testObject.registerMixIn("pojoProtected", Pojo.class, PojoProtected1.class);

		testObject.getPojoList()[0].setProtectedString("This text should not be serialized if protected by PojoProtected1");
		assertEquals(testObject.toJsonNode("pojoProtected").get("pojoList").get(0).has("protectedString"), false);
	}
	
	/**
	 * Various assumptions are made for this abstraction to work properly 
	 * and they need to be proven correct or we're going to have a bad time
	 */
	@Test
	public void testNaiveAssumptions() throws Exception {
		// I would expect that I can update ephemeral nodes, is that true?
		byte[] testBytes1 = "first test".getBytes();
		byte[] testBytes2 = "another test".getBytes();		
		curator.create().creatingParentsIfNeeded().withMode(		
			 CreateMode.EPHEMERAL).forPath("/test1", testBytes1);		
		curator.setData().forPath("/test1", testBytes2);
		
		// I would expect that versions still increment when updating ephemeral		
		curator.create().creatingParentsIfNeeded().withMode(		
			 CreateMode.PERSISTENT).forPath("/test2", testBytes1);		
		assertEquals(curator.checkExists().forPath("/test2").getVersion(), 0);
		assertEquals(curator.setData().forPath("/test2", testBytes2).getVersion(), 1);
		
		// I would expect that closing a curator removes an ephermal when there
		// are still other curators created from the same factory/connection string		
		CuratorFramework tempCurator = buildCuratorFramework();		
		tempCurator.create().creatingParentsIfNeeded().withMode(
			 CreateMode.EPHEMERAL).forPath("/test3", testBytes1);
		assertEquals(curator.checkExists().forPath("/test3").getVersion(), 0);
		tempCurator.close();
		Thread.sleep(10 * 1000); // Give the ephemeral time to timeout
		assertEquals(curator.checkExists().forPath("/test3"), null);
		
		LOG.info("Looks like our assumptions are solid");
	}

	@AfterClass
	public static void cleanup() throws Exception {
		curator.close();
		zk.shutdown();
	}

}