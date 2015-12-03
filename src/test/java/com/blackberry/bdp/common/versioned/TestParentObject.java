/**
 * Copyright 2014 BlackBerry, Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you 
 * may not use this file except in compliance with the License. You may 
 * obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 */
package com.blackberry.bdp.common.versioned;

//import com.fasterxml.jackson.annotation.JsonInclude;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.apache.curator.framework.CuratorFramework;

public class TestParentObject extends ZkVersioned<TestParentObject> {

	private long longObject;
	private List<TestObject> testObjects;
	
	public TestParentObject() { }
	
	public TestParentObject(CuratorFramework curator, String zkPath) {
		super(curator, zkPath);
	}
	
	/**
	 * @return the longObject
	 */
	@JsonProperty public long getLongObject() {
		return longObject;
	}

	/**
	 * @param longObject the longObject to set
	 */
	public void setLongObject(long longObject) {
		this.longObject = longObject;
	}

	/**
	 * @return the testObjects
	 */
	public List<TestObject> getTestObjects() {
		return testObjects;
	}

	/**
	 * @param testObjects the testObjects to set
	 */
	public void setTestObjects(List<TestObject> testObjects) {
		this.testObjects = testObjects;
	}

}
