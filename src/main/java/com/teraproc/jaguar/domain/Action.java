package com.teraproc.jaguar.domain;

import com.teraproc.jaguar.provider.manager.ApplicationManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Action for framework
 */
public class Action {
  private JaguarUser user;
  private long applicationId;
  private long policyId;
  // action definition is parsed by action plugin
  private String definition;
  private List<String> targets;
  private int cooldown;
  private long lastAction;
  private ApplicationManager applicationManager;

  public Action(String definition) {
    this.definition = definition;
    targets = new ArrayList<>();
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

  public List<String> getTargets() {
    return targets;
  }

  public void setTargets(List<String> targets) {
    this.targets = targets;
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
