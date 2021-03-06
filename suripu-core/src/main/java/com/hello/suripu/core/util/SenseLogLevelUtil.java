package com.hello.suripu.core.util;

/**
 * Created by jnorgan on 10/27/15.
 */
public class SenseLogLevelUtil {

  //As defined in https://github.com/hello/kitsune/blob/master/kitsune/uart_logger.h
  public final static Integer LOG_INFO = 0x01;
  public final static Integer LOG_WARNING = 0x02;
  public final static Integer LOG_ERROR = 0x04;
  public final static Integer LOG_TIME = 0x08;
  public final static Integer LOG_RADIO = 0x10;
  public final static Integer LOG_FACTORY = 0x40;
  public final static Integer LOG_TOP = 0x80;
  public final static Integer LOG_DEBUG = 0x400;

  public final static Integer LOG_VIEW_ONLY = 0x20;
  public final static Integer LOG_AUDIO = 0x100;
  public final static Integer LOG_PROX = 0x200;

  public final static Integer DEFAULT_LOG_LEVEL = LOG_INFO | LOG_WARNING | LOG_ERROR | LOG_FACTORY | LOG_TOP;
  public final static Integer WARNING_LOG_LEVEL = LOG_WARNING | LOG_INFO;
  public final static Integer ERROR_LOG_LEVEL = LOG_ERROR | WARNING_LOG_LEVEL;
  public final static Integer DEBUG_LOG_LEVEL = LOG_DEBUG | LOG_TOP | LOG_FACTORY | LOG_RADIO | LOG_TIME | ERROR_LOG_LEVEL;

  public enum LogLevel {
    DEFAULT(SenseLogLevelUtil.DEFAULT_LOG_LEVEL),
    INFO(SenseLogLevelUtil.DEFAULT_LOG_LEVEL),
    WARNING(SenseLogLevelUtil.WARNING_LOG_LEVEL),
    ERROR(SenseLogLevelUtil.ERROR_LOG_LEVEL),
    DEBUG(SenseLogLevelUtil.DEBUG_LOG_LEVEL);

    public Integer value;

    private LogLevel(Integer value) {
      this.value = value;
    }

  }

  public static Boolean isAllowedLogLevel(final Integer logLevel) {

    return (logLevel.equals(SenseLogLevelUtil.DEFAULT_LOG_LEVEL) ||
        logLevel.equals(SenseLogLevelUtil.WARNING_LOG_LEVEL) ||
        logLevel.equals(SenseLogLevelUtil.ERROR_LOG_LEVEL) ||
        logLevel.equals(SenseLogLevelUtil.DEBUG_LOG_LEVEL));
  }

  public static LogLevel getLogLevelFromString(final String val) {
    final LogLevel[] logLevels = LogLevel.values();

    for (final LogLevel logLevel : logLevels) {
      if (logLevel.toString().equals(val)) {
        return logLevel;
      }
    }

    throw new IllegalArgumentException(String.format("%s is not a valid LogLevel", val));
  }
}


