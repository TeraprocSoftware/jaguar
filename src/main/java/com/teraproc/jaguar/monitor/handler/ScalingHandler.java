package com.teraproc.jaguar.monitor.handler;

import com.google.common.annotations.VisibleForTesting;
import com.teraproc.jaguar.domain.Action;
import com.teraproc.jaguar.log.JaguarLoggerFactory;
import com.teraproc.jaguar.log.Logger;
import com.teraproc.jaguar.monitor.event.ScalingEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.util.concurrent.ExecutorService;

@Component
public class ScalingHandler implements ApplicationListener<ScalingEvent> {
  private static final DecimalFormat TIME_FORMAT = new DecimalFormat("##.##");
  private static final int MIN_IN_MS = 1000 * 60;

  private static final Logger LOGGER =
      JaguarLoggerFactory.getLogger(ScalingHandler.class);

  @Autowired
  private ExecutorService executorService;

  @Autowired
  private ApplicationContext applicationContext;

  @Override
  public void onApplicationEvent(ScalingEvent event) {
    Action action = event.getAction();
    long remainingTime = getRemainingCooldownTime(action);
    if (remainingTime > 0) {
      LOGGER.info(action.getPolicyId(),
          "Action '{}' cannot be triggered. The application is in the middle of"
              + " cooling down, which will be completed in '{}' min(s)",
              action.getDefinition(), TIME_FORMAT
                  .format((double) remainingTime / MIN_IN_MS));
      return;
    }
    executorService.execute(getScalingRequest(action));
  }

  @VisibleForTesting
  protected ScalingRequest getScalingRequest(Action action) {
    return (ScalingRequest) applicationContext.getBean(
        "ScalingRequest", action);
  }

  private long getRemainingCooldownTime(Action action) {
    int coolDown = action.getCooldown();
    long lastAction = action.getLastAction();
    return lastAction == 0 ? 0
        : (coolDown * MIN_IN_MS) - (System.currentTimeMillis()
            - lastAction);
  }
}
