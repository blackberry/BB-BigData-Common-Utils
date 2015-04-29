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

import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IgnoreNonVersionedIntrospector extends JacksonAnnotationIntrospector {
	private static final Logger LOG = LoggerFactory.getLogger(IgnoreNonVersionedIntrospector.class);
	
	@Override
	public boolean hasIgnoreMarker(final AnnotatedMember m) {
		LOG.info("Checking annotated member {}", m.getName());
		
		if (m.hasAnnotation(VersionedAttribute.class)) {
			LOG.info("member {} has anno", m.getName());
			return false;
		}
		else
		{
			LOG.info("member {} DOES NOT have anno", m.getName());
			return true;
		}
	}
	
}
