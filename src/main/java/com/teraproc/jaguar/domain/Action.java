package com.teraproc.jaguar.domain;

import com.teraproc.jaguar.provider.manager.ApplicationManager;

/**
 * Action for framework
 */
public class Action {
  private JaguarUser user;
  private long applicationId;
  private long policyId;
  // action definition is parsed by action plugin
  private String definition;
  private String target;
  private int cooldown;
  private long lastAction;
  private ApplicationManager applicationManager;

  public Action(String definition) {
    this.definition = definition;
  }

  public JaguarUser getUser() {
    return user;
  }

  public void setUser(JaguarUser user) {
    this.user = user;
  }

  public long getApplicationId() {
    return applicationId;
  }

  public void setApplicationId(long applicationId) {
    this.applicationId = applicationId;
  }

  public ApplicationManager getApplicationManager() {
    return this.applicationManager;
  }

  public void setApplicationManager(ApplicationManager applicationManager) {
    this.applicationManager = applicationManager;
  }

  public long getPolicyId() {
    return policyId;
  }

  public void setPolicyId(long policyId) {
    this.policyId = policyId;
  }

  public String getDefinition() {
    return definition;
  }

  public void setDefinition(String definition) {
    this.definition = definition;
  }

  public String getTarget() {
    return target;
  }

  public void setTarget(String target) {
    this.target = target;
  }

  public int getCooldown() {
    return cooldown;
  }

  public void setCooldown(int cooldown) {
    this.cooldown = cooldown;
  }

  public long getLastAction() {
    return lastAction;
  }

  public void setLastAction(long lastAction) {
    this.lastAction = lastAction;
  }

  public synchronized void setLastScalingActionCurrent() {
    this.lastAction = System.currentTimeMillis();
  }
}
