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

import com.blackberry.bdp.common.jmx.MetricRegistrySingleton;
import org.apache.logging.log4j.core.config.ConfigurationListener;
import org.apache.logging.log4j.core.config.Reconfigurable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dariens
 */
public class InstrumentedLoggerSingleton implements ConfigurationListener
{
	private static final Logger LOG = LoggerFactory.getLogger(InstrumentedLoggerSingleton.class);
	private static InstrumentedLoggerSingleton instance = null;
	
	private final InstrumentedAppender appender;
	private final Filter filter = null;
	private final PatternLayout layout = null;
	
	public static synchronized InstrumentedLoggerSingleton getInstance()
	{
		if (instance == null)
		{
			instance = new InstrumentedLoggerSingleton();			
		}
		
		return instance;
	}

	private InstrumentedLoggerSingleton()
	{
		appender = new InstrumentedAppender(MetricRegistrySingleton.getInstance().getMetricsRegistry(), filter, layout, false);
		appender.start();		
		instrument();
	}
	
	private void instrument()
	{
		LoggerContext context = (LoggerContext) LogManager.getContext(false);
		Configuration config = context.getConfiguration();

		config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME).addAppender(appender, Level.ALL, filter);
		config.addListener(this);
		context.updateLoggers(config);		
		
		LOG.info("Logging configuration has been modified and a new InstrumentedAppender has been configured on the root logger");		
	}
	
	public void onChange(Reconfigurable reconfigurable)
	{
		instrument();
	}
}