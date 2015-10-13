package com.teraproc.jaguar.monitor.event;

import com.teraproc.jaguar.domain.Action;
import org.springframework.context.ApplicationEvent;

public class ScalingEvent extends ApplicationEvent {

  public ScalingEvent(Action action) {
    super(action);
  }

  public Action getAction() {
    return (Action) getSource();
  }

}
