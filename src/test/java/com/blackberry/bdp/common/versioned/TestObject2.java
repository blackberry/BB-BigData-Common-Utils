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

public class TestObject2 extends ZkVersioned {

	private long longObject2 = Long.MAX_VALUE;
	private String stringObject2 = "String 2";
	private Boolean booleanObject2 = Boolean.TRUE;
	private Integer integerObject2 = Integer.MAX_VALUE;
	private Byte byteObject2 = Byte.MAX_VALUE;
	private Short shortObject2 = Short.MAX_VALUE;
	private Double doubleObject2 = Double.MAX_VALUE;
	private Character charObject2 = Character.MAX_VALUE;

	public TestObject2() {

	}

	/**
	 * @return the longObject2
	 */
	public long getLongObject2() {
		return longObject2;
	}

	/**
	 * @param longObject2 the longObject2 to set
	 */
	public void setLongObject2(long longObject2) {
		this.longObject2 = longObject2;
	}

	/**
	 * @return the stringObject2
	 */
	public String getStringObject2() {
		return stringObject2;
	}

	/**
	 * @param stringObject2 the stringObject2 to set
	 */
	public void setStringObject2(String stringObject2) {
		this.stringObject2 = stringObject2;
	}

	/**
	 * @return the booleanObject2
	 */
	public Boolean getBooleanObject2() {
		return booleanObject2;
	}

	/**
	 * @param booleanObject2 the booleanObject2 to set
	 */
	public void setBooleanObject2(Boolean booleanObject2) {
		this.booleanObject2 = booleanObject2;
	}

	/**
	 * @return the integerObject2
	 */
	public Integer getIntegerObject2() {
		return integerObject2;
	}

	/**
	 * @param integerObject2 the integerObject2 to set
	 */
	public void setIntegerObject2(Integer integerObject2) {
		this.integerObject2 = integerObject2;
	}

	/**
	 * @return the byteObject2
	 */
	public Byte getByteObject2() {
		return byteObject2;
	}

	/**
	 * @param byteObject2 the byteObject2 to set
	 */
	public void setByteObject2(Byte byteObject2) {
		this.byteObject2 = byteObject2;
	}

	/**
	 * @return the shortObject2
	 */
	public Short getShortObject2() {
		return shortObject2;
	}

	/**
	 * @param shortObject2 the shortObject2 to set
	 */
	public void setShortObject2(Short shortObject2) {
		this.shortObject2 = shortObject2;
	}

	/**
	 * @return the doubleObject2
	 */
	public Double getDoubleObject2() {
		return doubleObject2;
	}

	/**
	 * @param doubleObject2 the doubleObject2 to set
	 */
	public void setDoubleObject2(Double doubleObject2) {
		this.doubleObject2 = doubleObject2;
	}

	/**
	 * @return the charObject2
	 */
	public Character getCharObject2() {
		return charObject2;
	}

	/**
	 * @param charObject2 the charObject2 to set
	 */
	public void setCharObject2(Character charObject2) {
		this.charObject2 = charObject2;
	}
}