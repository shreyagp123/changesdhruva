package com.cisco.dhruva.util.log;

import com.cisco.dhruva.util.log.event.Event.ErrorType;
import com.cisco.dhruva.util.log.event.Event.EventSubType;
import com.cisco.dhruva.util.log.event.Event.EventType;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import org.slf4j.MDC;
import org.slf4j.event.Level;

public class DhruvaLogger implements Logger {

  private static final String EVENT_TYPE = "eventType";
  private static final String EVENT_SUBTYPE = "eventSubType";
  private org.slf4j.Logger logger;

  public DhruvaLogger(org.slf4j.Logger logger) {
    this.logger = logger;
  }

  @Override
  public void error(String message, Throwable throwable) {

    logger.error(message, throwable);
  }

  @Override
  public void error(String message) {
    logger.error(message);
  }

  @Override
  public void info(String message, Object object) {
    logger.info(message, object);
  }

  @Override
  public void info(String message) {
    logger.info(message);
  }

  @Override
  public void info(String format, Object arg1, Object arg2) {
    logger.info(format, arg1, arg2);
  }

  @Override
  public void info(String format, Supplier<?>... suppliers) {
    logger.info(format, getAllLamda(suppliers));
  }

  @Override
  public void info(String format, Object... arguments) {
    logger.info(format, arguments);
  }

  @Override
  public void warn(String message, Throwable throwable) {
    logger.warn(message, throwable);
  }

  @Override
  public void warn(String message, Object... arguments) {
    logger.warn(message, arguments);
  }

  @Override
  public void warn(String format, Object arg1, Object arg2) {
    logger.warn(format, arg1, arg2);
  }

  @Override
  public void error(String format, Object arg1, Object arg2) {
    logger.error(format, arg1, arg2);
  }

  @Override
  public void error(String format, Object... arguments) {
    logger.error(format, arguments);
  }

  @Override
  public void error(String format, Supplier<?>... suppliers) {
    logger.error(format, getAllLamda(suppliers));
  }

  @Override
  public void debug(String message) {
    logger.debug(message);
  }

  @Override
  public void debug(String format, Object... arguments) {
    logger.debug(format, arguments);
  }

  @Override
  public void debug(String format, Supplier<?>... suppliers) {
    logger.debug(format, getAllLamda(suppliers));
  }

  @Override
  public void emitEvent(
      EventType eventType,
      EventSubType eventSubType,
      ErrorType errorType,
      String message,
      Map<String, String> additionalKeyValueInfo) {

    Map<String, String> contextMapCopy = MDC.getCopyOfContextMap();

    MDC.put(EVENT_TYPE, eventType.name());
    if (eventSubType != null) {
      MDC.put(EVENT_TYPE, eventSubType.name());
    }
    if (errorType != null) {
      MDC.put("ErrorType", errorType.name());
    }
    if (additionalKeyValueInfo != null) {
      additionalKeyValueInfo.forEach((key, value) -> MDC.put(key, value));
    }

    logger.info(message);

    MDC.clear();
    if (contextMapCopy != null && !contextMapCopy.isEmpty()) {
      MDC.setContextMap(contextMapCopy);
    }
  }

  @Override
  public void setMDC(String key, String value) {
    MDC.put(key, value);
  }

  @Override
  public void setMDC(Map<String, String> map) {
    map.forEach((key, value) -> MDC.put(key, value));
  }

  @Override
  public void clearMDC() {
    MDC.clear();
  }

  @Override
  public Map<String, String> getMDCMap() {
    Map<String, String> mdcMap = MDC.getCopyOfContextMap();
    return mdcMap == null ? new HashMap<String, String>() : mdcMap;
  }

  @Override
  public String getName() {
    return logger.getName();
  }

  // Should be removed , adding for compatible now
  @Override
  public boolean isEnabled(Level level) {
    boolean isEnabled = false;
    switch (level) {
      case INFO:
        isEnabled = logger.isInfoEnabled();
        break;
      case WARN:
        isEnabled = logger.isWarnEnabled();
        break;
      case DEBUG:
        isEnabled = logger.isDebugEnabled();
        break;
      case ERROR:
        isEnabled = logger.isErrorEnabled();
        break;
    }
    return isEnabled;
  }

  // Should be removed , adding for compatible now
  @Override
  public void log(Level level, String message) {
    switch (level) {
      case INFO:
        logger.info(message);
        break;
      case WARN:
        logger.warn(message);
        break;
      case DEBUG:
        logger.debug(message);
        break;
      case ERROR:
        logger.error(message);
        break;
    }
  }

  // Should be removed , adding for compatible now
  @Override
  public void log(Level level, String message, Throwable throwable) {
    switch (level) {
      case INFO:
        logger.info(message);
        break;
      case WARN:
        logger.warn(message, throwable);
        break;
      case DEBUG:
        logger.debug(message);
        break;
      case ERROR:
        logger.error(message, throwable);
        break;
    }
  }

  // Should be removed , adding for compatible now
  @Override
  public boolean isDebugEnabled() {
    return logger.isDebugEnabled();
  }

  // Should be removed , adding for compatible now
  @Override
  public boolean isWarnEnabled() {
    return logger.isWarnEnabled();
  }

  // Should be removed , adding for compatible now
  @Override
  public boolean isInfoEnabled() {
    return logger.isInfoEnabled();
  }

  // Should be removed once we have lamda support in Slf4j
  private Object[] getAllLamda(Supplier<?>[] suppliers) {
    if (suppliers == null) {
      return null;
    } else {
      Object[] arguements = new Object[suppliers.length];

      for (int i = 0; i < arguements.length; ++i) {
        arguements[i] = suppliers[i].get();
      }

      return arguements;
    }
  }
}
