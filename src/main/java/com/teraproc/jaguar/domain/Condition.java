package com.teraproc.jaguar.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Condition {
  private String expression;
  private List<String> metrics;
  // <aggregateFunc, <metricName, aggregatedValue>>
  private Map<String, Map<String, Number>> aggregates;
  private String componentName;
  private EvalMethod evalMethod;
  private String threshold;

  public Condition() {
    metrics = new ArrayList<>();
    setAggregates();
  }

  public Condition(String expr) {
    this.expression = expr;
  }

  public String getExpression() {
    return expression;
  }

  public List<String> getMetrics() {
    return metrics;
  }

  public void setMetrics(List<String> metrics) {
    this.metrics = metrics;
  }

  public void setExpression(String expression) {
    this.expression = expression;
  }


  public Map<String, Map<String, Number>> getAggregates() {
    return aggregates;
  }

  public void setAggregates() {
    aggregates = new HashMap<>();
    aggregates.put("max", new HashMap<String, Number>());
    aggregates.put("min", new HashMap<String, Number>());
    aggregates.put("sum", new HashMap<String, Number>());
    aggregates.put("avg", new HashMap<String, Number>());
    aggregates.put("any", new HashMap<String, Number>());
    aggregates.put("first", new HashMap<String, Number>());
    aggregates.put("last", new HashMap<String, Number>());
  }

  public String getComponentName() {
    return componentName;
  }

  public void setComponentName(String componentName) {
    this.componentName = componentName;
  }

  public EvalMethod getEvalMethod() {
    return evalMethod;
  }

  public void setEvalMethod(EvalMethod evalMethod) {
    this.evalMethod = evalMethod;
  }

  public String getThreshold() {
    return threshold;
  }

  public void setThreshold(String threshold) {
    this.threshold = threshold;
  }

  public String toString() {
    return "componentName=" + componentName + " evalMethod=" + evalMethod
        + " threshold=" + threshold + " expression=" + expression;
  }
}
