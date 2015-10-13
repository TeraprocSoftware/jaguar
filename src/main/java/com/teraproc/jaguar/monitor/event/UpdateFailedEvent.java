package com.teraproc.jaguar.monitor.event;

import org.springframework.context.ApplicationEvent;

public class UpdateFailedEvent extends ApplicationEvent {

  public UpdateFailedEvent(long serviceId) {
    super(serviceId);
  }

  public long getServiceId() {
    return (long) getSource();
  }
}