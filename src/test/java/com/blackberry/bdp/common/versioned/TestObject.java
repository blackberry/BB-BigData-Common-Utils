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

public class TestObject extends ZkVersioned<TestObject> {

	private long longObject;
	private String stringObject;
	private List<Pojo> pojoList;
	
	private String sensitive1;
	private String sensitive2;
	
	public TestObject() { }
	
	public TestObject(CuratorFramework curator, String zkPath) {
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
	 * @return the stringObject
	 */
	public String getStringObject() {
		return stringObject;
	}

	/**
	 * @param stringObject the stringObject to set
	 */
	public void setStringObject(String stringObject) {
		this.stringObject = stringObject;
	}

	/**
	 * @return the pojoList
	 */
	@JsonProperty public List<Pojo> getPojoList() {
		return pojoList;
	}

	/**
	 * @param pojoList
	 */
	public void setPojoList(List<Pojo> pojoList) {
		this.pojoList = pojoList;
	}

	/**
	 * @return the sensitive
	 */
	public String getSensitive1() {
		return sensitive1;
	}

	/**
	 * @param sensitive1 the sensitive to set
	 */
	public void setSensitive1(String sensitive1) {
		this.sensitive1 = sensitive1;
	}

	/**
	 * @return the sensitive2
	 */
	public String getSensitive2() {
		return sensitive2;
	}

	/**
	 * @param sensitive2 the sensitive2 to set
	 */
	public void setSensitive2(String sensitive2) {
		this.sensitive2 = sensitive2;
	}

}
