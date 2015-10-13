package com.teraproc.jaguar.provider.manager.slider;

public class SliderAppComponent {

  private String componentName;
  private int memory;
  private int vcores;
  private int priority;
  private int requestedInstances;
  private int actualInstances;

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
}
