package com.teraproc.jaguar.monitor.evaluator;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

import java.util.Map;

public interface EvaluatorExecutor
    extends ApplicationEventPublisher, ApplicationEventPublisherAware,
    Runnable {

  void setContext(Map<String, Object> context);

}
