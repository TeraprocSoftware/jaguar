package com.teraproc.jaguar.monitor;

public final class MonitorUpdateRate {
  /**
   * Every 10 seconds.
   */
  public static final String METRIC_UPDATE_RATE_CRON = "0/10 * * * * ?";

  public static final int METRIC_UPDATE_INTERVAL = 10;

  private MonitorUpdateRate() {
    throw new IllegalStateException();
  }
}
