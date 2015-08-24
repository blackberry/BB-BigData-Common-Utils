/**
 * Copyright 2015 BlackBerry, Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.blackberry.bdp.common.conversion;

import static com.blackberry.bdp.common.conversion.Converter.*;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.Assert.*;

public class ConverterTest {

	private static final Logger LOG = LoggerFactory.getLogger(ConverterTest.class);
	
	@Test
	public void testGetBytesFromInt() {
		byte[] maxBytes = getBytes(Integer.MAX_VALUE);
		byte[] minBytes = getBytes(Integer.MIN_VALUE);
		
		assertEquals(intFromBytes(maxBytes), Integer.MAX_VALUE);
		assertEquals(intFromBytes(minBytes), Integer.MIN_VALUE);
		LOG.info("converting ints to/from bytes works");
	}
	
}
