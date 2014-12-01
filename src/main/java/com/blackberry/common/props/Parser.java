/**
 * Copyright 2014 BlackBerry, Limited.
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

package com.blackberry.common.props;

import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Parser
{

	private static final Logger LOG = LoggerFactory.getLogger(Parser.class);
	private final Properties props;
	
	public Parser(Properties props)
	{
		this.props = props;
	}
	
	public Long parseLong(String propertyName) throws Exception
	{
		Long returnValue;
		
		if (props.containsKey(propertyName))
		{
			returnValue = Long.parseLong(props.getProperty(propertyName));
		}
		else
		{
			throw new Exception("missing property: " + propertyName);
		}
		
		return returnValue;
	}
	
	public Long  parseLong(String propertyName, Long defaultValue) throws Exception
	{
		Long returnValue = defaultValue;
		
		if (props.containsKey(propertyName))
		{
			returnValue = Long.parseLong(props.getProperty(propertyName));
		}
		
		return returnValue;
	}	
		
	public Integer parseInteger(String propertyName) throws Exception
	{
		Integer returnValue;
		
		if (props.containsKey(propertyName))
		{
			returnValue = Integer.parseInt(props.getProperty(propertyName));
		}
		else
		{
			throw new Exception("missing property: " + propertyName);
		}
		
		return returnValue;
	}
	
	public Integer parseInteger(String propertyName, Integer defaultValue) throws Exception
	{
		Integer returnValue = defaultValue;
		
		if (props.containsKey(propertyName))
		{
			returnValue = Integer.parseInt(props.getProperty(propertyName));
		}
		
		return returnValue;
	}	

	public Boolean parseBoolean(String propertyName) throws Exception
	{
		Boolean returnValue;
		
		if (props.containsKey(propertyName))
		{
			String property = props.getProperty(propertyName);
			if (property.toLowerCase().equals("true") || property.toLowerCase().equals("false"))
			{
				returnValue = Boolean.parseBoolean(property.toLowerCase());
			}
			else
			{
				throw new Exception("property " + property + "must be either 'true' or 'false', not " + property, null);
			}
		}
		else
		{
			throw new Exception("missing property: " + propertyName);
		}
		
		return returnValue;
	}
	
	public Boolean parseBoolean(String propertyName, Boolean defaultValue) throws Exception
	{
		Boolean returnValue = defaultValue;
		
		if (props.containsKey(propertyName))
		{
			String property = props.getProperty(propertyName);
			if (property.toLowerCase().equals("true") || property.toLowerCase().equals("false"))
			{
				returnValue = Boolean.parseBoolean(property.toLowerCase());
			}
			else
			{
				throw new Exception("property " + property + "must be either 'true' or 'false', not " + property, null);
			}
		}
		
		return returnValue;
	}	

	public String parseString(String propertyName) throws Exception
	{
		String returnValue;
		
		if (props.containsKey(propertyName))
		{
			returnValue = props.getProperty(propertyName);
		}
		else
		{
			throw new Exception("missing property: " + propertyName);
		}
		
		return returnValue;
	}
	
	public String parseString(String propertyName, String defaultValue) throws Exception
	{
		String returnValue = defaultValue;
		
		if (props.containsKey(propertyName))
		{
			returnValue = props.getProperty(propertyName);		
		}
		
		return returnValue;
	}	
}
