package com.teraproc.jaguar.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

public class AggregateUtilsTest {

  @Test
  public void testMax() {
    assertEquals(
        3, AggregateUtils.max(Arrays.asList(new Number[]{1, 2, 3, 1, 2, 3})));
    assertEquals(
        3.0,
        AggregateUtils.max(Arrays.asList(new Number[]{1.0, 2, 1, 3.0, 2.0})));
  }

  @Test
  public void testMin() {
    assertEquals(
        1, AggregateUtils.min(
            Arrays.asList(new Number[]{1, 2, 3, 1, 2, 3})));
    assertEquals(
        1.0, AggregateUtils.min(
            Arrays.asList(new Number[]{1.0, 2, 1, 3.0, 2.0})));
  }

  @Test
  public void testSum() {
    assertEquals(
        12.0, AggregateUtils.sum(
            Arrays.asList(new Number[]{1, 2, 3, 1, 2, 3})).doubleValue(),
        0.0001);
    assertEquals(
        9.0, AggregateUtils.sum(
            Arrays.asList(new Number[]{1.0, 2, 1, 3.0, 2.0})).doubleValue(),
        0.0001);
  }

  @Test
  public void testAvg() {
    assertEquals(
        2.0, AggregateUtils.avg(
            Arrays.asList(new Number[]{1, 2, 3, 1, 2, 3})), 0.0001);
    assertEquals(
        1.8, AggregateUtils.avg(
            Arrays.asList(new Number[]{1.0, 2, 1, 3.0, 2.0})), 0.0001);
  }

  @Test
  public void testFirst() {
    assertEquals(
        1, AggregateUtils.first(
            Arrays.asList(new Number[]{1, 2, 3, 3, 1, 2})));
    assertEquals(
        1.0, AggregateUtils.first(
            Arrays.asList(new Number[]{1.0, 2, 1, 3.0, 2.0})));
  }

  @Test
  public void testLast() {
    assertEquals(
        2, AggregateUtils.last(
            Arrays.asList(new Number[]{1, 2, 3, 3, 1, 2})));
    assertEquals(
        2.0, AggregateUtils.last(
            Arrays.asList(new Number[]{1.0, 2, 1, 3.0, 2.0})));
  }

  @Test
  public void testAny() {
    assertEquals(
        2, AggregateUtils.any(
            Arrays.asList(new Number[]{1, 2, 3, 3, 1, 2})));
    assertEquals(
        2.0, AggregateUtils.any(
            Arrays.asList(new Number[]{1.0, 2, 1, 3.0, 2.0})));
  }
}
