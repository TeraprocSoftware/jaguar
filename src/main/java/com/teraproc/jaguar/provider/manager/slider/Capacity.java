package com.teraproc.jaguar.provider.manager.slider;

public class Capacity {
  private int min;
  private int max;
  private int adjustment;

  public int getMin() {
    return min;
  }

  public void setMin(int min) {
    this.min = min;
  }

  public int getMax() {
    return max;
  }

  public void setMax(int max) {
    this.max = max;
  }

  public int getAdjustment() {
    return adjustment;
  }

  public void setAdjustment(int adjustment) {
    this.adjustment = adjustment;
  }

  public String toString() {
    return "adjustment=" + String.valueOf(adjustment) + " min=" + String
        .valueOf(min) + " max=" + String.valueOf(max);
  }
}
