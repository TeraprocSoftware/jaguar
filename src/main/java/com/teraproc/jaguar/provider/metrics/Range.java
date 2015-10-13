package com.teraproc.jaguar.provider.metrics;

import java.util.HashMap;
import java.util.Map;

public class Range {
  private Map<String, TimeRange> range;

  public Range() {
    range = new HashMap<>();
  }

  public Map<String, TimeRange> getRange() {
    return range;
  }

  public void setRange(
      Map<String, TimeRange> range) {
    this.range = range;
  }
}
