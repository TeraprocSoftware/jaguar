package com.teraproc.jaguar.provider.manager.slider;

import java.util.HashMap;
import java.util.Map;

public class SliderActionDefinition {
  private String componentName;
  private int cooldown;
  private AdjustmentType adjustmentType;
  private Map<ResourceType, Capacity> scalingAdjustment = new HashMap<>();

  public String getComponentName() {
    return componentName;
  }

  public void setComponentName(String componentName) {
    this.componentName = componentName;
  }

  public int getCooldown() {
    return cooldown;
  }

  public void setCooldown(int cooldown) {
    this.cooldown = cooldown;
  }

  public AdjustmentType getAdjustmentType() {
    return adjustmentType;
  }

  public void setAdjustmentType(AdjustmentType adjustmentType) {
    this.adjustmentType = adjustmentType;
  }

  public Map<ResourceType, Capacity> getScalingAdjustment() {
    return scalingAdjustment;
  }

  public void setScalingAdjustment(
      Map<ResourceType, Capacity> scalingAdjustment) {
    this.scalingAdjustment = scalingAdjustment;
  }
}
