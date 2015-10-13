package com.teraproc.jaguar.utils;

import com.teraproc.jaguar.log.JaguarLoggerFactory;
import com.teraproc.jaguar.log.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.quartz.CronExpression;

import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;

public final class DateUtils {

  private static final Logger LOGGER =
      JaguarLoggerFactory.getLogger(DateUtils.class);

  private DateUtils() {
    throw new IllegalStateException();
  }

  public static boolean isActiveTime(
      long policyId, String timeZone, String cron, String start,
      long duration) {
    try {
      // check date
      CronExpression datePatternCron = getCronExpression("* * * " + cron);
      datePatternCron.setTimeZone(TimeZone.getTimeZone(timeZone));
      Date currentTime = getCurrentDate(timeZone);
      if (!datePatternCron.isSatisfiedBy(currentTime)) {
        return false;
      }

      // check time
      CronExpression startTimeCron =
          getCronExpression(getStartCronExpr(cron, start));
      startTimeCron.setTimeZone(TimeZone.getTimeZone(timeZone));
      Date checkPoint = new Date(currentTime.getTime() - duration * 1000);
      Date lastTime = startTimeCron.getNextValidTimeAfter(checkPoint);
      DateTime lastDateTime = getDateTime(lastTime, timeZone);
      DateTime currentDateTime = getDateTime(currentTime, timeZone);
      return
          (currentDateTime.toDate().getTime() - lastDateTime.toDate().getTime())
              < duration * 1000 ? true : false;
    } catch (ParseException e) {
      LOGGER.warn(policyId, "Invalid cron expression, {}", e.getMessage());
      return false;
    }
  }

  public static CronExpression getCronExpression(String cron)
      throws ParseException {
    return new CronExpression(cron);
  }

  private static String getStartCronExpr(String cron, String start) {
    // start format is hh:mm:ss
    return start.substring(6, 7) + " " + start.substring(3, 4) + " " + start
        .substring(0, 1) + " " + cron;
  }

  private static DateTime getDateTime(Date date, String timeZone) {
    return new DateTime(date).withZone(getTimeZone(timeZone));
  }

  private static Date getCurrentDate(String timeZone) {
    return getCurrentDateTime(timeZone).toLocalDateTime().toDate();
  }

  private static DateTime getCurrentDateTime(String timeZone) {
    return DateTime.now(getTimeZone(timeZone));
  }

  public static DateTimeZone getTimeZone(String timeZone) {
    return DateTimeZone.forID(timeZone);
  }

}
