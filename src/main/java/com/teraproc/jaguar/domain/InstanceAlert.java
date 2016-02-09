package com.teraproc.jaguar.domain;

import java.util.HashMap;
import java.util.Map;

public class InstanceAlert extends BaseAlert {
  private String componentName;
  private Condition condition;
  private Map<String, Integer> latestSuccessiveIntervals;

  public InstanceAlert() {
    latestSuccessiveIntervals = new HashMap<>();
    condition = new Condition();
  }

  public String getComponentName() {
    return componentName;
  }

  public void setComponentName(String componentName) {
    this.componentName = componentName;
  }

  public Condition getCondition() {
    return condition;
  }

  public void setCondition(Condition condition) {
    this.condition = condition;
  }

  public Map<String, Integer> getLatestSuccessiveIntervals() {
    return latestSuccessiveIntervals;
  }

  public void setLatestSuccessiveIntervals(
      Map<String, Integer> intervals) {
    latestSuccessiveIntervals = intervals;
  }
}
