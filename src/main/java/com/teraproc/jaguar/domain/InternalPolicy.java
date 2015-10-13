package com.teraproc.jaguar.domain;

import java.util.List;

public class InternalPolicy {
  private Policy policy;
  private BaseAlert alert;
  private List<Action> actions;
  private long durationInSeconds;
  private int intervalAccumulator;

  public InternalPolicy(Policy policy) {
    this.policy = policy;
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
