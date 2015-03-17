/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.blackberry.bdp.common.logger;

import com.codahale.metrics.log4j2.InstrumentedAppender;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.layout.PatternLayout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackberry.bdp.common.jmx.MetricRegistrySingleton;

/**
 *
 * @author dariens
 */
public class InstrumentedLoggerSingleton
{
	private static InstrumentedLoggerSingleton instance = null;
	
	private InstrumentedLoggerSingleton()
	{
		// Private to prevent instantiation
		
		Filter filter = null;
		PatternLayout layout = null;

		InstrumentedAppender appender = new InstrumentedAppender(MetricRegistrySingleton.getInstance().getMetricsRegistry(), filter, layout, false);
		appender.start();
		
		LoggerContext context = (LoggerContext) LogManager.getContext(false);
		Configuration config = context.getConfiguration();
		
		config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME).addAppender(appender, Level.ALL, filter);
		context.updateLoggers(config);		
	}
	
	public static synchronized InstrumentedLoggerSingleton getInstance()
	{
		if (instance == null)
		{
			instance = new InstrumentedLoggerSingleton();			
		}
		
		return instance;
	}
	
	public Logger getLogger(String name)
	{
		return LoggerFactory.getLogger(name);
	}
}