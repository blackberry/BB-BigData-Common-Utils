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

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;


public class TestObject1 extends ZkVersioned {

	private long longObject1;
	private String stringObject1;
	private Boolean booleanObject1;
	private Integer integerObject1;
	private Byte byteObject1;
	private Short shortObject1;
	private Double doubleObject1;
	private Character charObject1;
	
	private TestObject2[] testObject2List;

	public TestObject1() {

	}

	/**
	 * @return the longObject1
	 */
	@JsonProperty public long getLongObject1() {
		return longObject1;
	}

	/**
	 * @param longObject1 the longObject1 to set
	 */
	public void setLongObject1(long longObject1) {
		this.longObject1 = longObject1;
	}

	/**
	 * @return the stringObject1
	 */
	public String getStringObject1() {
		return stringObject1;
	}

	/**
	 * @param stringObject1 the stringObject1 to set
	 */
	public void setStringObject1(String stringObject1) {
		this.stringObject1 = stringObject1;
	}

	/**
	 * @return the booleanObject1
	 */
	public Boolean getBooleanObject1() {
		return booleanObject1;
	}

	/**
	 * @param booleanObject1 the booleanObject1 to set
	 */
	public void setBooleanObject1(Boolean booleanObject1) {
		this.booleanObject1 = booleanObject1;
	}

	/**
	 * @return the integerObject1
	 */
	public Integer getIntegerObject1() {
		return integerObject1;
	}

	/**
	 * @param integerObject1 the integerObject1 to set
	 */
	public void setIntegerObject1(Integer integerObject1) {
		this.integerObject1 = integerObject1;
	}

	/**
	 * @return the byteObject1
	 */
	public Byte getByteObject1() {
		return byteObject1;
	}

	/**
	 * @param byteObject1 the byteObject1 to set
	 */
	public void setByteObject1(Byte byteObject1) {
		this.byteObject1 = byteObject1;
	}

	/**
	 * @return the shortObject1
	 */
	public Short getShortObject1() {
		return shortObject1;
	}

	/**
	 * @param shortObject1 the shortObject1 to set
	 */
	public void setShortObject1(Short shortObject1) {
		this.shortObject1 = shortObject1;
	}

	/**
	 * @return the doubleObject1
	 */
	public Double getDoubleObject1() {
		return doubleObject1;
	}

	/**
	 * @param doubleObject1 the doubleObject1 to set
	 */
	public void setDoubleObject1(Double doubleObject1) {
		this.doubleObject1 = doubleObject1;
	}

	/**
	 * @return the charObject1
	 */
	public Character getCharObject1() {
		return charObject1;
	}

	/**
	 * @param charObject1 the charObject1 to set
	 */
	public void setCharObject1(Character charObject1) {
		this.charObject1 = charObject1;
	}

	/**
	 * @return the testObject2List
	 */
	public TestObject2[] getTestObject2List() {
		return testObject2List;
	}

	/**
	 * @param testObject2List the testObject2List to set
	 */
	public void setTestObject2List(TestObject2[] testObject2List) {
		this.testObject2List = testObject2List;
	}

}
