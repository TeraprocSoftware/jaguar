package com.teraproc.jaguar.monitor;

import com.teraproc.jaguar.domain.Application;
import com.teraproc.jaguar.monitor.evaluator.EvaluatorExecutor;
import com.teraproc.jaguar.service.ApplicationService;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.ExecutorService;

public abstract class AbstractMonitor implements Monitor {

  private ApplicationService applicationService;
  private ApplicationContext applicationContext;
  private ExecutorService executorService;

  @Override
  public void execute(JobExecutionContext context)
      throws JobExecutionException {
    evalContext(context);
    for (Application application : applicationService.findAllEnabled()) {
      EvaluatorExecutor evaluatorExecutor = applicationContext
          .getBean(getEvaluatorType().getSimpleName(), EvaluatorExecutor.class);
      evaluatorExecutor.setContext(getContext(application));
      executorService.submit(evaluatorExecutor);
    }
  }

  private void evalContext(JobExecutionContext context) {
    JobDataMap monitorContext = context.getJobDetail().getJobDataMap();
    applicationContext = (ApplicationContext) monitorContext
        .get(MonitorContext.APPLICATION_CONTEXT.name());
    executorService = applicationContext.getBean(ExecutorService.class);
    applicationService = applicationContext.getBean(ApplicationService.class);
  }

}
