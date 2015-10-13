package com.teraproc.jaguar.log;

public final class JaguarLoggerFactory {

  private JaguarLoggerFactory() {
    throw new IllegalStateException();
  }

  public static Logger getLogger(Class clazz) {
    return new Logger(clazz);
  }

}
