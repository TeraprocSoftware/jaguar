package com.teraproc.jaguar.utils;

import java.util.List;

public class NumberUtils {
  private NumberUtils() {
    throw new IllegalStateException();
  }

  public static boolean isNumeric(String str) {
    try {
      Double.parseDouble(str);
      return true;
    } catch (NumberFormatException nfe) {
      try {
        Integer.parseInt(str);
        return true;
      } catch (NumberFormatException e) {
        return false;
      }
    }
  }

  public static boolean evaluateAnd(List<Boolean> items) {
    for (boolean item : items) {
      if (!item) {
        return false;
      }
    }
    return true;
  }

  public static boolean evaluateOr(List<Boolean> items) {
    for (boolean item : items) {
      if (item) {
        return true;
      }
    }
    return false;
  }
}
