package com.teraproc.jaguar.utils;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class NumberUtilsTest {
  @Test
  public void testIsNumeric() throws Exception {
    assertEquals(true, NumberUtils.isNumeric("0"));
    assertEquals(true, NumberUtils.isNumeric("1.1"));
    assertEquals(true, NumberUtils.isNumeric("-1"));
    assertEquals(true, NumberUtils.isNumeric("-1.1"));
    assertEquals(false, NumberUtils.isNumeric("a"));
    assertEquals(false, NumberUtils.isNumeric("abc"));
    assertEquals(false, NumberUtils.isNumeric("+-"));
  }

  @Test
  public void testEvaluateAnd() throws Exception {
    assertEquals(
        true, NumberUtils.evaluateAnd(
            Arrays.asList(new Boolean[]{true, true, true})));
    assertEquals(
        false, NumberUtils.evaluateAnd(
            Arrays.asList(new Boolean[]{true, false, true})));
    assertEquals(
        false, NumberUtils.evaluateAnd(
            Arrays.asList(new Boolean[]{false, false})));
  }

  @Test
  public void testEvaluateOr() throws Exception {
    assertEquals(
        true, NumberUtils.evaluateOr(
            Arrays.asList(new Boolean[]{true, true, true})));
    assertEquals(
        true, NumberUtils.evaluateOr(
            Arrays.asList(new Boolean[]{true, false, true})));
    assertEquals(
        false, NumberUtils.evaluateOr(
            Arrays.asList(new Boolean[]{false, false})));
  }
}
