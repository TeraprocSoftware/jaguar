package com.teraproc.jaguar.monitor;

import com.teraproc.jaguar.domain.Application;
import org.quartz.Job;

import java.util.Map;

public interface Monitor extends Job {

  String getIdentifier();

  String getTriggerExpression();

  Class getEvaluatorType();

  Map<String, Object> getContext(Application application);

}
