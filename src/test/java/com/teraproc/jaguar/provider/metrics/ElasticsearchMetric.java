package com.teraproc.jaguar.provider.metrics;

import java.util.Map;

/**
 * From hadoop-metrics-elasticsearch-sink
 */
public class ElasticsearchMetric {
  private long updateTime;
  private String appName;
  private String hostName;
  private String cmptName;
  private String containerId;
  private String context;
  private Map<String, String> metrics;

  public long getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(long timestamp) {
    this.updateTime = timestamp;
  }

  public String getAppName() {
    return appName;
  }

  public void setAppName(String appName) {
    this.appName = appName;
  }

  public String getHostName() {
    return hostName;
  }

  public void setHostName(String hostName) {
    this.hostName = hostName;
  }

  public String getCmptName() {
    return cmptName;
  }

  public void setCmptName(String context) {
    this.cmptName = context;
  }

  public String getContainerId() {
    return containerId;
  }

  public void setContainerId(String containerId) {
    this.containerId = containerId;
  }

  public String getContext() {
    return context;
  }

  public void setContext(String context) {
    this.context = context;
  }

  public Map<String, String> getMetrics() {
    return metrics;
  }

  public void setMetrics(Map<String, String> metrics) {
    this.metrics = metrics;
  }
}
