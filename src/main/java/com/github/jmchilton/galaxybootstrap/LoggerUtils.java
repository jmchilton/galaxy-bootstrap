package com.github.jmchilton.galaxybootstrap;

import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class used to setup Logger properties.
 */
public class LoggerUtils {
  
  private static final Logger logger = LoggerFactory
      .getLogger(LoggerUtils.class);
  
  /**
   * Allows adjustement of logging level by setting a system property.
   * For example "-Dlog4j.logger.com.github.jmchilton.galaxybootstrap=DEBUG"
   * Code from http://stackoverflow.com/a/16862953
   */
  public static void configureLog4jFromSystemProperties()
  {
    final String LOGGER_PREFIX = "log4j.logger.";

    for(String propertyName : System.getProperties().stringPropertyNames())
    {
      if (propertyName.startsWith(LOGGER_PREFIX)) {
        String loggerName = propertyName.substring(LOGGER_PREFIX.length());
        String levelName = System.getProperty(propertyName, "");
        Level level = Level.toLevel(levelName); // defaults to DEBUG
        if (!"".equals(levelName) && !levelName.toUpperCase().equals(level.toString())) {
          logger.error("Skipping unrecognized log4j log level " + levelName + ": -D" + propertyName + "=" + levelName);
          continue;
        }
        logger.info("Setting " + loggerName + " => " + level.toString());
        org.apache.log4j.Logger.getLogger(loggerName).setLevel(level);
      }
    }
  }
}
