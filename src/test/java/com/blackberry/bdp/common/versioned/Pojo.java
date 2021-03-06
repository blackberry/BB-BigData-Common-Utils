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

public class Pojo {

	private String publicString;
	private String protectedString;
	
	public Pojo() {

	}	

	public Pojo(String publicString, String protectedString) {
		this.publicString = publicString;
		this.protectedString = protectedString;
	}	

	/**
	 * @return the publicString
	 */
	public String getPublicString() {
		return publicString;
	}

	/**
	 * @param publicString the publicString to set
	 */
	public void setPublicString(String publicString) {
		this.publicString = publicString;
	}

	/**
	 * @return the protectedString
	 */
	public String getProtectedString() {
		return protectedString;
	}

	/**
	 * @param protectedString the protectedString to set
	 */
	public void setProtectedString(String protectedString) {
		this.protectedString = protectedString;
	}
}