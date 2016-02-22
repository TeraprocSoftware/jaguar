package com.teraproc.jaguar.log;

import org.slf4j.LoggerFactory;

public class Logger implements JaguarLogger {

  public static final int NOT_SERVICE_RELATED = -1;
  private static final String L_DECORATOR = " [";
  private static final String R_DECORATOR = "] ";
  private final org.slf4j.Logger sl4jLogger;

  public Logger(Class clazz) {
    this.sl4jLogger = LoggerFactory.getLogger(clazz);
  }

  public Logger(String name) {
    this.sl4jLogger = LoggerFactory.getLogger(name);
  }

  @Override
  public String getName() {
    return sl4jLogger.getName();
  }

  @Override
  public boolean isTraceEnabled() {
    return sl4jLogger.isTraceEnabled();
  }

  @Override
  public void trace(long serviceId, String msg) {
    sl4jLogger.trace(getPrefix(serviceId) + msg);
  }

  @Override
  public void trace(long serviceId, String format, Object arg) {
    sl4jLogger.trace(getPrefix(serviceId) + format, arg);
  }

  @Override
  public void trace(long serviceId, String format, Object arg1, Object arg2) {
    sl4jLogger.trace(getPrefix(serviceId) + format, arg1, arg2);
  }

  @Override
  public void trace(long serviceId, String format, Object... arguments) {
    sl4jLogger.trace(getPrefix(serviceId) + format, arguments);
  }

  @Override
  public void trace(long serviceId, String msg, Throwable t) {
    sl4jLogger.trace(getPrefix(serviceId) + msg, t);
  }

  @Override
  public boolean isDebugEnabled() {
    return sl4jLogger.isDebugEnabled();
  }

  @Override
  public void debug(long serviceId, String msg) {
    sl4jLogger.debug(getPrefix(serviceId) + msg);
  }

  @Override
  public void debug(long serviceId, String format, Object arg) {
    sl4jLogger.debug(getPrefix(serviceId) + format, arg);
  }

  @Override
  public void debug(long serviceId, String format, Object arg1, Object arg2) {
    sl4jLogger.debug(getPrefix(serviceId) + format, arg1, arg2);
  }

  @Override
  public void debug(long serviceId, String format, Object... arguments) {
    sl4jLogger.debug(getPrefix(serviceId) + format, arguments);
  }

  @Override
  public void debug(long serviceId, String msg, Throwable t) {
    sl4jLogger.debug(getPrefix(serviceId) + msg, t);
  }

  @Override
  public boolean isInfoEnabled() {
    return sl4jLogger.isInfoEnabled();
  }

  @Override
  public void info(long serviceId, String msg) {
    sl4jLogger.info(getPrefix(serviceId) + msg);
  }

  @Override
  public void info(long serviceId, String format, Object arg) {
    sl4jLogger.info(getPrefix(serviceId) + format, arg);
  }

  @Override
  public void info(long serviceId, String format, Object arg1, Object arg2) {
    sl4jLogger.info(getPrefix(serviceId) + format, arg1, arg2);
  }

  @Override
  public void info(long serviceId, String format, Object... arguments) {
    sl4jLogger.info(getPrefix(serviceId) + format, arguments);
  }

  @Override
  public void info(long serviceId, String msg, Throwable t) {
    sl4jLogger.info(getPrefix(serviceId) + msg, t);
  }

  @Override
  public boolean isWarnEnabled() {
    return sl4jLogger.isWarnEnabled();
  }

  @Override
  public void warn(long serviceId, String msg) {
    sl4jLogger.warn(getPrefix(serviceId) + msg);
  }

  @Override
  public void warn(long serviceId, String format, Object arg) {
    sl4jLogger.warn(getPrefix(serviceId) + format, arg);
  }

  @Override
  public void warn(long serviceId, String format, Object... arguments) {
    sl4jLogger.warn(getPrefix(serviceId) + format, arguments);
  }

  @Override
  public void warn(long serviceId, String format, Object arg1, Object arg2) {
    sl4jLogger.warn(getPrefix(serviceId) + format, arg1, arg2);
  }

  @Override
  public void warn(long serviceId, String msg, Throwable t) {
    sl4jLogger.warn(getPrefix(serviceId) + msg, t);
  }

  @Override
  public boolean isErrorEnabled() {
    return sl4jLogger.isErrorEnabled();
  }

  @Override
  public void error(long serviceId, String msg) {
    sl4jLogger.error(getPrefix(serviceId) + msg);
  }

  @Override
  public void error(long serviceId, String format, Object arg) {
    sl4jLogger.error(getPrefix(serviceId) + format, arg);
  }

  @Override
  public void error(long serviceId, String format, Object arg1, Object arg2) {
    sl4jLogger.error(getPrefix(serviceId) + format, arg1, arg2);
  }

  @Override
  public void error(long serviceId, String format, Object... arguments) {
    sl4jLogger.error(getPrefix(serviceId) + format, arguments);
  }

  @Override
  public void error(long serviceId, String msg, Throwable t) {
    sl4jLogger.error(getPrefix(serviceId) + msg, t);
  }

  private String getPrefix(long serviceId) {
    if (NOT_SERVICE_RELATED == serviceId) {
      return "";
    }
    return L_DECORATOR + serviceId + R_DECORATOR;
  }

}
