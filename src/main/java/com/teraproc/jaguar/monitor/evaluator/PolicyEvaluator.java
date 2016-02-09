package com.teraproc.jaguar.monitor.evaluator;

import com.teraproc.jaguar.domain.Action;
import com.teraproc.jaguar.domain.Application;
import com.teraproc.jaguar.domain.Condition;
import com.teraproc.jaguar.domain.EvalMethod;
import com.teraproc.jaguar.domain.GroupAlert;
import com.teraproc.jaguar.domain.InstanceAlert;
import com.teraproc.jaguar.domain.InternalPolicy;
import com.teraproc.jaguar.domain.Policy;
import com.teraproc.jaguar.log.JaguarLoggerFactory;
import com.teraproc.jaguar.log.Logger;
import com.teraproc.jaguar.monitor.MonitorUpdateRate;
import com.teraproc.jaguar.monitor.event.ScalingEvent;
import com.teraproc.jaguar.monitor.event.UpdateFailedEvent;
import com.teraproc.jaguar.provider.metrics.ElasticsearchClientProvider;
import com.teraproc.jaguar.service.ApplicationService;
import com.teraproc.jaguar.service.PolicyService;
import com.teraproc.jaguar.utils.AggregateUtils;
import com.teraproc.jaguar.utils.DateUtils;
import com.teraproc.jaguar.utils.NumberUtils;
import com.teraproc.jaguar.utils.TreeNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("PolicyEvaluator")
@Scope("prototype")
public class PolicyEvaluator extends AbstractEventPublisher
    implements EvaluatorExecutor {

  private static final Logger LOGGER =
      JaguarLoggerFactory.getLogger(PolicyEvaluator.class);
  private static ScriptEngineManager mgr = new ScriptEngineManager();
  private static ScriptEngine engine = mgr.getEngineByName("JavaScript");

  @Autowired
  private ApplicationService applicationService;

  @Autowired
  private PolicyService policyService;

  @Autowired
  private ElasticsearchClientProvider elasticsearchClient;

  private long applicationId;
  private long policyId;
  private int policyInterval;

  @Override
  public void setContext(Map<String, Object> context) {
    applicationId = (long) context.get(EvaluatorContext.SERVICE_ID.name());
  }

  @Override
  public void run() {
    for (InternalPolicy internal : policyService
        .getInternalPolicies(applicationId)) {
      Policy policy = internal.getPolicy();
      policyId = policy.getId();
      policyInterval = policy.getInterval();

      // policy evaluator runs every 10 seconds
      // policy interval is arbitrary
      internal.addIntervalAccumulator(1);
      // policy interval is not reached
      if (internal.getIntervalAccumulator()
          < policyInterval / MonitorUpdateRate.METRIC_UPDATE_INTERVAL) {
        continue;
      }
      // policy interval is reached, clear accumulator
      internal.setIntervalAccumulator(0);
      LOGGER.info(
          policyId, "Start checking application '{}' policy: '{}'",
          applicationId, policy.getName());

      try {
        // check policy's active time window
        if (DateUtils.isActiveTime(
            policy.getId(), policy.getTimeZone(), policy.getCron(),
            policy.getStartTime(), internal.getDurationInSeconds())) {
          LOGGER.info(
              policy.getId(), "Policy '{}' is active at current time",
              policy.getName());
          evaluatePolicy(internal);
        } else {
          // when active time window is closed, clear history of how many
          // successive intervals the alert is true
          clearSuccessiveIntervals(internal);
        }
      } catch (Exception e) {
        LOGGER.error(policyId, "Failed to evaluate policy due to: ", e);
        publishEvent(new UpdateFailedEvent(policy.getId()));
      }
    }
  }

  protected void evaluatePolicy(InternalPolicy internal) {
    if (internal.getPolicy().getScope()
        .equals(com.teraproc.jaguar.domain.Scope.GROUP)) {
      evaluateGroupPolicy(internal);
    } else {
      evaluateInstancePolicy(internal);
    }
  }

  protected void evaluateInstancePolicy(InternalPolicy internal) {
    List<String> instances =
        evaluateInstanceAlert((InstanceAlert) internal.getAlert());
    // no instance alert is true
    if (instances.size() == 0) {
      clearSuccessiveIntervals(internal);
      return;
    }

    LOGGER.info(
        policyId, "Instances '{}' evaluate alert as true",
        instances.toString());
    addSuccessiveIntervals(internal, instances);
    LOGGER.info(
        policyId, "Successive intervals is '{}'"
        , ((InstanceAlert) internal.getAlert()).getLatestSuccessiveIntervals()
            .toString());

    // trigger instance action if greater than successive interval threshold
    List<String> targets = new ArrayList<>();
    for (String instance : instances) {
      if (((InstanceAlert) internal.getAlert()).getLatestSuccessiveIntervals()
          .get(instance) >= internal.getAlert().getSuccessiveIntervals()) {
        targets.add(instance);
      }
    }
    // instance alert is triggered
    if (targets.size() > 0) {
      LOGGER.info(
          policyId,
          "Instances '{}' have successive intervals equal to or greater than "
              + "threshold '{}'",
          targets.toString(), internal.getAlert().getSuccessiveIntervals());
      for (Action action : internal.getActions()) {
        for (String target: targets) {
          publishInstanceScalingEvent(action, target);
        }
      }
    }
  }

  protected void evaluateGroupPolicy(InternalPolicy internal) {
    if (evaluateGroupAlert((GroupAlert) internal.getAlert())) {
      LOGGER.info(
          policyId, "Group alert evaluation result: true");

      addSuccessiveIntervals(internal, null);
      LOGGER.info(
          policyId, "Successive intervals is '{}'",
          ((GroupAlert) internal.getAlert()).getLatestSuccessiveIntervals());

      // trigger action if greater than successive interval threshold
      if (((GroupAlert) internal.getAlert()).getLatestSuccessiveIntervals()
          >= internal.getAlert().getSuccessiveIntervals()) {
        LOGGER.info(
            policyId,
            "Successive intervals is equal to or greater than threshold '{}'"
            , internal.getAlert().getSuccessiveIntervals());
        for (Action action : internal.getActions()) {
          publishGroupScalingEvent(action);
        }
      }
    } else {
      // clear history successive intervals when alert is false
      clearSuccessiveIntervals(internal);
    }
  }

  private void publishInstanceScalingEvent(
      Action action, String target) {
    // TODO:
    // We need to clone the action, otherwise there will be race condition
    // between this thread, and event processing thread.
    action.setTarget(target);
    action.setUser(getApplication().getUser());
    action.setApplicationId(applicationId);
    action.setPolicyId(policyId);
    publishEvent(new ScalingEvent(action));
  }

  protected void publishGroupScalingEvent(Action action) {
    action.setUser(getApplication().getUser());
    action.setApplicationId(applicationId);
    action.setPolicyId(policyId);
    publishEvent(new ScalingEvent(action));
  }

  private void clearSuccessiveIntervals(InternalPolicy internal) {
    if (internal.getPolicy().getScope()
        .equals(com.teraproc.jaguar.domain.Scope.GROUP)) {
      ((GroupAlert) internal.getAlert()).setLatestSuccessiveIntervals(0);
    } else if (
        ((InstanceAlert) internal.getAlert()).getLatestSuccessiveIntervals()
            != null) {
      ((InstanceAlert) internal.getAlert()).getLatestSuccessiveIntervals()
          .clear();
    }
  }

  /**
   * Group alerts records one successive interval number
   * <p/>
   * Instance alert records successive intervals for each instance
   * For example, application has 4 instances {a, b, c, d}
   * Before this interval:
   * InternalPolicy.latestSuccessiveIntervals = {a:1, b:2}
   * In this interval:
   * 2 instances are evaluated as true {b, d}
   * After this interval:
   * InternalPolicy.latestSuccessiveIntervals = {b:3, d:1}
   */
  private void addSuccessiveIntervals(
      InternalPolicy internal, List<String> instances) {
    if (internal.getPolicy().getScope()
        .equals(com.teraproc.jaguar.domain.Scope.GROUP)) {
      ((GroupAlert) internal.getAlert()).addLastestSuccessiveIntervals(1);
    } else {
      Map<String, Integer> instanceMap = new HashMap<>();
      for (String instance : instances) {
        instanceMap.put(instance, 1);
      }
      for (String newInstance : ((InstanceAlert) internal.getAlert())
          .getLatestSuccessiveIntervals().keySet()) {
        if (instanceMap.containsKey(newInstance)) {
          int original = ((InstanceAlert) internal.getAlert())
              .getLatestSuccessiveIntervals().get(newInstance);
          instanceMap.put(newInstance, ++original);
        }
      }
      ((InstanceAlert) internal.getAlert())
          .setLatestSuccessiveIntervals(instanceMap);
    }
  }

  protected List<String> evaluateInstanceAlert(InstanceAlert alert) {
    List<String> instances = new ArrayList<>();
    try {
      Map<String, Boolean> instanceEvalResult =
          evaluateInstanceCondition(alert.getCondition());
      for (Map.Entry<String, Boolean> entry : instanceEvalResult.entrySet()) {
        if (entry.getValue()) {
          instances.add(entry.getKey());
        }
      }
    } catch (Exception e) {
      LOGGER.error(
          policyId, "Unable to evaluate alert '{}'",
          alert.getCondition().getExpression());
    }
    return instances;
  }

  protected boolean evaluateGroupAlert(GroupAlert alert) {
    try {
      return evaluateNode(alert.getRoot());
    } catch (Exception e) {
      LOGGER
          .error(
              policyId, "Unable to evaluate alert due to: {}", e.getMessage());
      return false;
    }
  }

  private boolean evaluateNode(TreeNode<Condition> node) throws Exception {
    if (node.hasChild()) {
      List<Boolean> items = new ArrayList<>();
      for (TreeNode child : node.getChildren()) {
        items.add(evaluateNode(child));
      }
      /* TODO: Optimize it by deferring the evaluation until needed. */
      return "and".equalsIgnoreCase(node.getData().getExpression())
          ? NumberUtils.evaluateAnd(items) : NumberUtils.evaluateOr(items);
    } else {
      return node.getData().getEvalMethod().equals(EvalMethod.PERCENT)
          ? evaluatePercentCondition(node.getData())
          : evaluateAggregateCondition(node.getData());
    }
  }

  private boolean evaluatePercentCondition(Condition condition)
      throws Exception {
    Map<String, Boolean> instanceEvalResult =
        evaluateInstanceCondition(condition);
    int trueNum = 0;
    for (Map.Entry<String, Boolean> entry : instanceEvalResult.entrySet()) {
      if (entry.getValue()) {
        trueNum++;
      }
    }
    int percentage = 100 * (trueNum / instanceEvalResult.size());
    return (boolean) engine.eval(percentage + condition.getThreshold());
  }

  private Map<String, Boolean> evaluateInstanceCondition(Condition condition)
      throws Exception {
    long current = System.currentTimeMillis();
    Map<String, Boolean> instanceAlertMap = new HashMap<>();
    // < containerId, < metricName, < metricValues >>>
    Map<String, Map<String, List<Number>>> allMetrics =
        elasticsearchClient.getInstanceMetrics(
            getApplication().getName(), condition.getComponentName()
            , new ArrayList<String>(condition.getMetrics()),
            current - policyInterval * 1000, current);

    /* TODO: Optimize it by only iterating the condition once */
    for (Map.Entry<String, Map<String, List<Number>>> entry : allMetrics
        .entrySet()) {
      String container = entry.getKey();
      Map<String, List<Number>> containerMetrics = entry.getValue();
      // calculate aggregated metrics
      for (Map.Entry<String, Map<String, Number>> entry1 : condition
          .getAggregates().entrySet()) {
        String func = entry1.getKey();
        for (Map.Entry<String, Number> entry2 : entry1.getValue().entrySet()) {
          String metricName = entry2.getKey();
          Collection<Number> values = containerMetrics.get(metricName);
          Method method =
              AggregateUtils.class.getMethod(func, Collection.class);
          Number aggregatedValue = (Number) method.invoke(null, values);
          entry2.setValue(aggregatedValue);
        }
      }

      String evalStr = condition.getExpression();
      // replace aggregated metrics with aggregated values
      for (Map.Entry<String, Map<String, Number>> entry1 : condition
          .getAggregates().entrySet()) {
        String func = entry1.getKey();
        for (Map.Entry<String, Number> entry2 : entry1.getValue().entrySet()) {
          String metricName = entry2.getKey();
          Number metricValue = entry2.getValue();
          String pattern = func + "( *)\\(" + metricName + "( *)\\)";
          evalStr = evalStr.replaceAll(pattern, metricValue.toString());
        }
      }

      // replace metrics names with values
      for (String metricName : condition.getMetrics()) {
        if (evalStr.contains(metricName) && containerMetrics
            .containsKey(metricName)) {
          evalStr = evalStr.replaceAll(
              metricName,
              AggregateUtils.last(containerMetrics.get(metricName)).toString());
        }
      }

      instanceAlertMap.put(container, (boolean) engine.eval(evalStr));
    }
    return instanceAlertMap;
  }

  private boolean evaluateAggregateCondition(Condition condition)
      throws Exception {
    long current = System.currentTimeMillis();
    Map<String, Map<String, Number>> allMetrics =
        elasticsearchClient.getMetricsLatestValue(
            getApplication().getName(), condition.getComponentName()
            , new ArrayList<String>(condition.getMetrics()),
            current - policyInterval * 1000, current);

    // calculate aggregated metrics
    for (Map.Entry<String, Map<String, Number>> entry : condition
        .getAggregates().entrySet()) {
      String func = entry.getKey();
      for (Map.Entry<String, Number> entry1 : entry.getValue().entrySet()) {
        String metricName = entry1.getKey();
        Collection<Number> values = allMetrics.get(metricName).values();
        Method method = AggregateUtils.class.getMethod(func, Collection.class);
        Number aggregatedValue = (Number) method.invoke(null, values);
        entry1.setValue(aggregatedValue);
      }
    }

    String evalStr = condition.getExpression();
    // replace aggregated metrics with aggregated values
    for (Map.Entry<String, Map<String, Number>> entry1 : condition
        .getAggregates().entrySet()) {
      String func = entry1.getKey();
      for (Map.Entry<String, Number> entry2 : entry1.getValue().entrySet()) {
        String metricName = entry2.getKey();
        Number metricValue = entry2.getValue();
        String pattern = func + "( *)\\(" + metricName + "( *)\\)";
        evalStr = evalStr.replaceAll(pattern, metricValue.toString());
      }
    }
    return (boolean) engine.eval(evalStr);
  }

  protected Application getApplication() {
    return applicationService.find(applicationId);
  }
}
