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

import com.google.common.base.Joiner;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
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
			throw new MissingPropertyException("missing property: " + propertyName, null);
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
			throw new MissingPropertyException("missing property: " + propertyName, null);
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
				throw new UnsupportedValueException("property " + property + "must be either 'true' or 'false', not " + property, null);
			}
		}
		else
		{
			throw new MissingPropertyException("missing property: " + propertyName, null);
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
				throw new UnsupportedValueException("property " + property + "must be either 'true' or 'false', not " + property, null);
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
			throw new MissingPropertyException("missing property: " + propertyName, null);
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
	
	/**
	 * Calls the object's setter method for the provided attribute type/value
	 * @param object
	 * @param attribute
	 * @param value
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException 
	 */
	public void set(Object object, 
		 String attribute, 
		 Object value) throws 
			NoSuchMethodException, 
			IllegalAccessException, 
			InvocationTargetException
	{
		String setter = String.format("set%C%s", attribute.charAt(0), attribute.substring(1));
		Method method = object.getClass().getMethod(setter, value.getClass());
		method.invoke(value, this, value);
	}
	
	/**
	 * 
	 * @param parserMethodName
	 * @param forClass
	 * @param hasDefault
	 * @return
	 * @throws NoSuchMethodException 
	 */
	
	public Method getParser(String parserMethodName, Class forClass, Object hasDefault) throws NoSuchMethodException
	{
		if (hasDefault != null)
		{
			return this.getClass().getMethod(parserMethodName, new Class[] {String.class, forClass});	
		}
		else
		{
			return getParser(parserMethodName, forClass);
		}
	}
	
	/**
	 * 
	 * @param parserMethodName
	 * @param forClass
	 * @return
	 * @throws NoSuchMethodException 
	 */
	
	public Method getParser(String parserMethodName, Class forClass) throws NoSuchMethodException
	{
		return this.getClass().getMethod(parserMethodName, new Class[] {String.class});
	}
	
	/**
	 * 
	 * @param object
	 * @param propertyName
	 * @param propertyClass
	 * @param supportedClass
	 * @param required
	 * @param defaultValue
	 * @param requiredValues
	 * @throws Exception 
	 */
	
	public void parseAndPopulate (
		 Object object, 
		 String propertyName, 
		 Class propertyClass, 
		 Class supportedClass,
		 Boolean required,
		 Object defaultValue,
		 ArrayList<Object> requiredValues) throws Exception
	{	
		String supportedClassName = supportedClass.getClass().getName();
		String propertyClassName = supportedClass.getClass().getName();
		
		if (object.getClass() != supportedClass)
		{	
			throw new Exception(String.format(
				 "Cannot apply property {} to class type {} when only {}class type(s) supported", 
				 propertyName, object.getClass().getName(), supportedClassName));
		}
		
		String parserMethodName = String.format("parse%C%s", propertyClassName.charAt(0), propertyClassName.substring(1));
		Method parserMethod = getParser(parserMethodName, propertyClass);
			
		if (parserMethod == null)
		{
			throw new Exception(String.format("Cannot find parser method {}", parserMethodName));				
		}
		
		Object propertyValue = null;
		
		try
		{			
			propertyValue = parserMethod.invoke(propertyName);
		}
		catch (Exception e)
		{
			if (e.getClass().getName().equals("MissingPropertyException"))
			{
				if (required && defaultValue == null)
				{
					throw e;
				}
				else if (required && defaultValue != null)
				{
					propertyValue = defaultValue;
				}
			}
		}
		
		if (requiredValues != null && !requiredValues.contains(propertyValue))
		{
			throw new UnsupportedValueException(String.format(
				 "{} unsupported value: {} must be one of {}", 
				propertyName, propertyValue, Joiner.on(", ").join(requiredValues)), null);
		}
		
	}
		
}
