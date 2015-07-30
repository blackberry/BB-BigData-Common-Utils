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

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
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
		return newCurator;
	}

	@BeforeClass
	public static void setup() throws Exception {
		zk = new LocalZkServer();
		curator = buildCuratorFramework();
		curator.start();
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

	@Test
	public void testSaveFetchNewObject() throws VersionMismatchException, Exception {
		TestObject testObject = getTestObject();
		String beforeSaveJson = testObject.toJSON();
		LOG.info("JSON before saving: {}", beforeSaveJson);
		testObject.save();
		TestObject retrievedObject = TestObject.get(TestObject.class, curator, "/testObject");
		LOG.info("JSON after saving: {}", retrievedObject.toJSON());
		assertEquals(beforeSaveJson, retrievedObject.toJSON());
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

	@AfterClass
	public static void cleanup() throws Exception {
		curator.close();
		zk.shutdown();
	}

}
