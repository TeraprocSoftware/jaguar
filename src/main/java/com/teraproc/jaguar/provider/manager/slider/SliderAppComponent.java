package com.teraproc.jaguar.provider.manager.slider;

import java.util.HashMap;
import java.util.Map;

public class SliderAppComponent {

  private String componentName;
  private int memory;
  private int vcores;
  private int priority;
  private int requestedInstances;
  private int actualInstances;
  private Map<String, Map<ResourceType, Integer>> containers;

  public SliderAppComponent() {
    containers = new HashMap<>();
  }

  public String getComponentName() {
    return componentName;
  }

  public void setComponentName(String componentName) {
    this.componentName = componentName;
  }

  public int getMemory() {
    return memory;
  }

  public void setMemory(int memory) {
    this.memory = memory;
  }

  public int getVcores() {
    return vcores;
  }

  public void setVcores(int vcores) {
    this.vcores = vcores;
  }

  public int getPriority() {
    return priority;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }

  public int getRequestedInstances() {
    return requestedInstances;
  }

  public void setRequestedInstances(int requestedInstances) {
    this.requestedInstances = requestedInstances;
  }

  public int getActualInstances() {
    return actualInstances;
  }

  public void setActualInstances(int actualInstances) {
    this.actualInstances = actualInstances;
  }

  public Map<String, Map<ResourceType, Integer>> getContainers() {
    return containers;
  }

  public void setContainers(
      Map<String, Map<ResourceType, Integer>> containers) {
    this.containers = containers;
  }
}
