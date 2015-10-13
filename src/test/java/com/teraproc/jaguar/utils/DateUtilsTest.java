package com.teraproc.jaguar.utils;

import org.junit.Test;

import java.text.ParseException;

import static org.junit.Assert.assertEquals;

public class DateUtilsTest {
  @Test
  public void testGetCronExpression() throws Exception {
    DateUtils.getCronExpression("* * * ? * MON-FRI");
    DateUtils.getCronExpression("0 0 14-6 ? * FRI-MON");
  }

  @Test(expected = ParseException.class)
  public void testInvalidCron() throws Exception {
    DateUtils.getCronExpression("abc");
    DateUtils.getCronExpression("* * * ? *");
    DateUtils.getCronExpression("* MON-FRI");
  }

  @Test
  public void testIsActiveTime() {
    assertEquals(
        true,
        DateUtils.isActiveTime(1L, "UTC", "? * MON-SUN", "00:00:00", 86400));
    assertEquals(
        true,
        DateUtils.isActiveTime(
            1L, "America/New_York", "? * MON-SUN", "00:00:00", 86400));
    assertEquals(
        true,
        DateUtils.isActiveTime(1L, "UTC", "? * MON-SUN", "00:00:00", 172800));
    /*assertEquals(
        false,
        DateUtils.isActiveTime(1L, "UTC", "? * SAT-SUN", "00:00:00", 0));
    assertEquals(
        false,
        DateUtils.isActiveTime(
            1L, "America/New_York", "? * SAT-SUN", "00:00:00", 0));*/
  }
}
