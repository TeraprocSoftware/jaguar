package com.teraproc.jaguar.monitor.handler;


import com.teraproc.jaguar.domain.Action;
import com.teraproc.jaguar.monitor.event.ScalingEvent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.ExecutorService;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TestScalingHandler {
  @Mock
  private ExecutorService executorService;
  @InjectMocks
  private ScalingHandler scalingHandler;

  private ScalingRequest scalingRequest = new ScalingRequest(null);

  @Before
  public void setup() throws Exception {
    scalingHandler = new ScalingHandler() {
      @Override
      protected ScalingRequest getScalingRequest(Action action) {
        return scalingRequest;
      }
    };
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testScalingActionExecuted() throws Exception {
    Action action = new Action("");
    action.setCooldown(1);
    action.setLastAction(0);
    Thread.sleep(100);
    ScalingEvent scalingEvent = new ScalingEvent(action);
    scalingHandler.onApplicationEvent(scalingEvent);
    verify(executorService, times(1)).execute(scalingRequest);
  }

  @Test
  public void testScalingActionNotExecutedWhenCoolDown() throws Exception {
    Action action = new Action("");
    action.setCooldown(1);
    action.setLastAction(System.currentTimeMillis());
    Thread.sleep(100);
    ScalingEvent scalingEvent = new ScalingEvent(action);
    scalingHandler.onApplicationEvent(scalingEvent);
    verify(executorService, never()).execute(scalingRequest);
  }
}
