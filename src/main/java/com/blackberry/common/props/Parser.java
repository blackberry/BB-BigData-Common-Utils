package com.blackberry.common.props;

import java.util.Properties;

public class Parser
{
	private final Properties props;
	
	public Parser(Properties props)
	{
		this.props = props;
	}
	
	public Boolean parseBoolean(String propertyName) throws Exception
	{
		return parseBoolean(propertyName, false);
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
}
