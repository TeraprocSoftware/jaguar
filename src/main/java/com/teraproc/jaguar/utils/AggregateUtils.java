package com.teraproc.jaguar.utils;

import java.util.Collection;

public class AggregateUtils {

  private AggregateUtils() {
    throw new IllegalStateException();
  }

  public static Number max(Collection<Number> list) {
    Number max = list.iterator().next();
    for (Number item : list) {
      if (item.floatValue() > max.floatValue()) {
        max = item;
      }
    }
    return max;
  }

  public static Number min(Collection<Number> list) {
    Number min = list.iterator().next();
    for (Number item : list) {
      if (item.floatValue() < min.floatValue()) {
        min = item;
      }
    }
    return min;
  }

  public static Number sum(Collection<Number> list) {
    Number sum = 0;
    for (Number item : list) {
      sum = sum.floatValue() + item.floatValue();
    }
    return sum;
  }

  public static Float avg(Collection<Number> list) {
    Number sum = 0;
    for (Number item : list) {
      sum = sum.floatValue() + item.floatValue();
    }
    return sum.floatValue() / list.size();
  }

  public static Number last(Collection<Number> list) {
    return (Number) list.toArray()[list.size() - 1];
  }

  public static Number first(Collection<Number> list) {
    return list.iterator().next();
  }

  public static Number any(Collection<Number> list) {
    return (Number) list.toArray()[list.size() - 1];
  }
}