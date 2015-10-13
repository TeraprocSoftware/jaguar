package com.teraproc.jaguar.monitor.handler;

import com.google.common.util.concurrent.AtomicLongMap;
import com.teraproc.jaguar.domain.Application;
import com.teraproc.jaguar.log.JaguarLoggerFactory;
import com.teraproc.jaguar.log.Logger;
import com.teraproc.jaguar.monitor.event.UpdateFailedEvent;
import com.teraproc.jaguar.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class UpdateFailedHandler
    implements ApplicationListener<UpdateFailedEvent> {

  private static final Logger LOGGER =
      JaguarLoggerFactory.getLogger(UpdateFailedHandler.class);
  private static final int RETRY_THRESHOLD = 5;

  @Autowired
  private ApplicationService applicationService;

  private final AtomicLongMap<Long> updateFailures = AtomicLongMap.create();

  @Override
  public void onApplicationEvent(UpdateFailedEvent event) {
    long id = event.getServiceId();
    Application application = applicationService.find(id);
    if (application == null) {
      return;
    }
    if (updateFailures.incrementAndGet(id) >= RETRY_THRESHOLD) {
      application.setEnabled(false);
      applicationService.save(application);
      updateFailures.remove(id);
      LOGGER.info(id, "Suspend monitoring for application '{}' due to failed"
          + " update attempts", application.getName());
    }
  }
}
