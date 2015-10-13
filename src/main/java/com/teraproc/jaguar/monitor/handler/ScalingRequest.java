package com.teraproc.jaguar.monitor.handler;

import com.teraproc.jaguar.domain.Action;
import com.teraproc.jaguar.domain.ActionProperties;
import com.teraproc.jaguar.domain.ActionStatus;
import com.teraproc.jaguar.domain.InstanceAlert;
import com.teraproc.jaguar.domain.InternalPolicy;
import com.teraproc.jaguar.domain.Policy;
import com.teraproc.jaguar.log.JaguarLoggerFactory;
import com.teraproc.jaguar.log.Logger;
import com.teraproc.jaguar.provider.manager.ActionException;
import com.teraproc.jaguar.service.HistoryService;
import com.teraproc.jaguar.service.PolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component("ScalingRequest")
@Scope("prototype")
public class ScalingRequest implements Runnable {

  private static final Logger LOGGER =
      JaguarLoggerFactory.getLogger(ScalingRequest.class);
  private Action action;

  @Autowired
  private HistoryService historyService;
  @Autowired
  private PolicyService policyService;

  public ScalingRequest(Action action) {
    this.action = action;
  }

  @Override
  public void run() {
    InternalPolicy internalPolicy = policyService.getInternalPolicy(
        action.getApplicationId(), action.getPolicyId());
    if (internalPolicy == null) {
      return;
    }
    String definition = action.getDefinition();
    try {
      LOGGER.info(internalPolicy.getId(), "Trigger action '{}'", definition);
      Properties props = action.getApplicationManager().performAction(
          action.getUser(), buildContext(internalPolicy), definition);
      if (props.get(ActionProperties.PROPERTY_STATUS)
          .equals(ActionStatus.NONE)) {
        return;
      }
      setLastScalingAction(internalPolicy.getPolicy(), definition, props);
    } catch (ActionException e) {
      LOGGER.error(internalPolicy.getId(),
          "Failed to execute action '{}': '{}'", definition, e);
      Properties props = new Properties();
      props.put(ActionProperties.PROPERTY_STATUS, ActionStatus.FAILED);
      props.put(ActionProperties.PROPERTY_STATUS_REASON,
          "Failed to trigger scaling action due to: " + e.getMessage());
      setLastScalingAction(internalPolicy.getPolicy(), definition, props);
    }
  }

  private Properties buildContext(InternalPolicy internalPolicy) {
    Properties context = new Properties();
    Policy policy = internalPolicy.getPolicy();
    context.setProperty(
        ActionProperties.PROPERTY_SCOPE, policy.getScope().toString());
    context.setProperty(
        ActionProperties.PROPERTY_POLICY_ID, String.valueOf(policy.getId()));
    context.setProperty(
        ActionProperties.PROPERTY_APPLICATION_NAME,
        policy.getApplication().getName());
    context.setProperty(
        ActionProperties.PROPERTY_APPLICATION_ID,
        String.valueOf(policy.getApplication().getId()));
    if (policy.getScope().equals(com.teraproc.jaguar.domain.Scope.INSTANCE)) {
      context.setProperty(
          ActionProperties.PROPERTY_COMPONENT_NAME,
          ((InstanceAlert) internalPolicy.getAlert()).getComponentName());
      // TODO:
      // The following logic is wrong, containerId got overwritten. We should
      // most likely pass in an Action object to the performAction() function,
      // so that we don't need to pass in a context.
      for (String containerId : action.getTargets()) {
        context.setProperty(
            ActionProperties.PROPERTY_CONTAINER_ID, containerId);
      }
    }
    return context;
  }

  private void setLastScalingAction(
      Policy policy, String definition, Properties properties) {
    historyService.createEntry(policy, definition, properties);
    action.setLastScalingActionCurrent();
  }
}
