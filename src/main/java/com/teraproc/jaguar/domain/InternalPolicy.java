package com.teraproc.jaguar.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InternalPolicy {
  private Policy policy;
  private BaseAlert alert;
  // action definition
  private List<Action> actions;
  // runtime actions
  private Map<String, Action> runtimeActions;
  private long durationInSeconds;
  private int intervalAccumulator;

  public InternalPolicy(Policy policy) {
    this.policy = policy;
    runtimeActions = new HashMap<>();
  }

  public Policy getPolicy() {
    return policy;
  }

  public void setPolicy(Policy policy) {
    this.policy = policy;
  }

  public long getId() {
    return this.policy.getId();
  }

  public List<Action> getActions() {
    return actions;
  }

  public void setActions(List<Action> actions) {
    this.actions = actions;
  }

  public Map<String, Action> getRuntimeActions() {
    return runtimeActions;
  }

  public void setRuntimeActions(
      Map<String, Action> runtimeActions) {
    this.runtimeActions = runtimeActions;
  }

  public BaseAlert getAlert() {
    return alert;
  }

  public void setAlert(BaseAlert baseAlert) {
    this.alert = baseAlert;
  }

  public long getDurationInSeconds() {
    return durationInSeconds;
  }

  public void setDurationInSeconds(long durationInSeconds) {
    this.durationInSeconds = durationInSeconds;
  }

  public int getIntervalAccumulator() {
    return intervalAccumulator;
  }

  public void setIntervalAccumulator(int count) {
    intervalAccumulator = count;
  }

  public void addIntervalAccumulator(int count) {
    intervalAccumulator = intervalAccumulator + count;
  }
}
