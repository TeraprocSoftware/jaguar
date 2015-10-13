package com.teraproc.jaguar.monitor;

import com.teraproc.jaguar.domain.Application;
import com.teraproc.jaguar.monitor.evaluator.EvaluatorContext;
import com.teraproc.jaguar.monitor.evaluator.PolicyEvaluator;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

@Component
public class MetricMonitor extends AbstractMonitor implements Monitor {

  @Override
  public String getIdentifier() {
    return "metric-monitor";
  }

  @Override
  public String getTriggerExpression() {
    return MonitorUpdateRate.METRIC_UPDATE_RATE_CRON;
  }

  @Override
  public Class getEvaluatorType() {
    return PolicyEvaluator.class;
  }

  @Override
  public Map<String, Object> getContext(Application application) {
    return Collections.<String, Object>singletonMap(
        EvaluatorContext.SERVICE_ID.name(), application.getId());
  }
}
