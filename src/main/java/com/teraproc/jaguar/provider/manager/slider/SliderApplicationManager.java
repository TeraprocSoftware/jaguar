package com.teraproc.jaguar.provider.manager.slider;

import com.teraproc.jaguar.domain.ActionProperties;
import com.teraproc.jaguar.domain.ActionStatus;
import com.teraproc.jaguar.domain.Provider;
import com.teraproc.jaguar.domain.JaguarUser;
import com.teraproc.jaguar.domain.Scope;
import com.teraproc.jaguar.log.JaguarLoggerFactory;
import com.teraproc.jaguar.log.Logger;
import com.teraproc.jaguar.provider.manager.ActionException;
import com.teraproc.jaguar.provider.manager.ApplicationManager;
import com.teraproc.jaguar.service.InvalidFormatException;
import com.teraproc.jaguar.service.NotFoundException;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static java.lang.Math.ceil;

@Service
public class SliderApplicationManager implements ApplicationManager {
  private static final Logger LOGGER =
      JaguarLoggerFactory.getLogger(SliderApplicationManager.class);
  private static final String PROPERTY_COMPONENT_NAME = "component";
  private static final String PROPERTY_CONTAINER_ID = "instance";
  private static final String PROPERTY_ORIGINAL_ALLOCATED =
      "original_allocated";
  private static final String PROPERTY_ORIGINAL_REQUESTED =
      "original_requested";
  private static final String PROPERTY_DESIRED = "desired";
  private static final String PROPERTY_ADJUSTMENT_TYPE = "adjustment_type";
  private static final String PROPERTY_ADJUSTMENT = "adjustment";
  private static final int MAX_CAPACITY = 100;

  @Autowired
  private SliderClientProxy sliderClientProxy;

  @Override
  public void validateApplication(JaguarUser user, String appName) {
    try {
      if (!sliderClientProxy.appExists(user, appName)) {
        throw new NotFoundException(
            "Application <" + appName + "> does not exist in slider");
      }
    } catch (YarnException | IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void validateApplicationComponent(
      JaguarUser user, String appName, String componentName) {
    try {
      if (!sliderClientProxy.appComptExists(
          user, appName, componentName)) {
        throw new NotFoundException(
            "Component <" + componentName + "> of application <"
                + appName + "> does not exist in slider");
      }
    } catch (YarnException | IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void validateAction(JaguarUser user, String app, String jsonDef) {
    try {
      SliderActionDefinition def = new ObjectMapper().readValue(
          jsonDef, SliderActionDefinition.class);
      if (!sliderClientProxy
          .appComptExists(user, app, def.getComponentName())) {
        throw new InvalidFormatException(
            "Invalid component name '" + def.getComponentName() + "' in action"
                + " definition");
      }
    } catch (YarnException | IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Properties performAction(
      JaguarUser user, Properties context, String jsonDef) {
    try {
      long policyId = Long.parseLong(
          context.getProperty(ActionProperties.PROPERTY_POLICY_ID));
      String appName =
          context.getProperty(ActionProperties.PROPERTY_APPLICATION_NAME);
      String cmptName =
          context.getProperty(ActionProperties.PROPERTY_COMPONENT_NAME);
      Scope scope =
          Scope.valueOf(context.getProperty(ActionProperties.PROPERTY_SCOPE));
      SliderActionDefinition def = new ObjectMapper().readValue(
          jsonDef, SliderActionDefinition.class);

      // scale-up/scale-down instance
      if (scope.equals(Scope.INSTANCE)) {
        String containerId = context.getProperty(PROPERTY_CONTAINER_ID);
        return scaleInstance(
            user, appName, cmptName, containerId, policyId, def);
      } else {
        // scale-out/scale-in application
        return scaleApplication(
            user, appName, def.getComponentName(), policyId, def);
      }
    } catch (Exception e) {
      throw new ActionException(e.getMessage(), e.getCause());
    }
  }

  @Override
  public Provider getProvider() {
    return Provider.SLIDER;
  }

  private Properties scaleInstance(
      JaguarUser user, String appName, String cmptName, String containerId,
      long policyId, SliderActionDefinition def) {
    // TODO: call slider to flex-up/flex-down
    return null;
  }

  private Properties scaleApplication(
      JaguarUser user, String appName, String cmptName, long policyId,
      SliderActionDefinition def)
      throws Exception {
    Properties recordProps = new Properties();
    recordProps.put(ActionProperties.PROPERTY_STATUS, ActionStatus.NONE);
    SliderApp app = sliderClientProxy.getSliderApp(user, appName);

    int requested = getRequestedNum(app, cmptName);
    int allocated = getAllocatedNum(app, cmptName);
    // Cannot get current resource usage from slider
    if (requested < 0 || allocated < 0) {
      LOGGER.warn(
          policyId,
          "No scaling activity due to: cannot get component instances from "
              + "slider application");
      return recordProps;
    }

    LOGGER.info(
        policyId,
        "Application '{}' component '{}' current instances: allocated '{}', "
            + "requested '{}'"
        , appName, def.getComponentName(), allocated, requested);
    int desired = getDesiredInstanceCount(
        def.getAdjustmentType(),
        def.getScalingAdjustment().get(ResourceType.COUNT), allocated);
    // Trigger manager if the desired resource is less than outstanding request
    if (requested != 0 && desired >= (requested + allocated)) {
      LOGGER.info(
          policyId,
          "No scaling activity due to: outstanding request '{}' has not been "
              + "allocated.",
          requested);
      return recordProps;
    } else if (requested == 0 && desired == (requested + allocated)) {
      LOGGER.info(
          policyId,
          "No scaling activity due to: allocated instances equals to desired "
              + "instances '{}'.",
          desired);
      return recordProps;
    }

    // Record manager properties
    recordProps.put(PROPERTY_COMPONENT_NAME, cmptName);
    recordProps
        .put(PROPERTY_ADJUSTMENT_TYPE, def.getAdjustmentType().toString());
    recordProps.put(
        PROPERTY_ADJUSTMENT,
        def.getScalingAdjustment().get(ResourceType.COUNT).toString());
    recordProps.put(
        PROPERTY_ORIGINAL_ALLOCATED,
        "instanceCount=" + String.valueOf(allocated));
    recordProps.put(
        PROPERTY_ORIGINAL_REQUESTED,
        "instanceCount=" + String.valueOf(requested));
    recordProps
        .put(PROPERTY_DESIRED, "instanceCount=" + String.valueOf(desired));
    try {
      LOGGER.info(
          policyId,
          "Sending request to change application '{}' component '{}' to '{}' "
              + "instance(s)",
          appName, def.getComponentName(), desired);
      Map<String, Integer> componentsMap = new HashMap<String, Integer>();
      componentsMap.put(def.getComponentName(), desired);
      sliderClientProxy.flexApp(user, appName, componentsMap);
      LOGGER.info(policyId, "Scaling manager is successfully triggered");
      recordProps.put(
          ActionProperties.PROPERTY_STATUS, ActionStatus.SUCCESS);
      recordProps.put(
          ActionProperties.PROPERTY_STATUS_REASON,
          "Scaling successfully triggered");
      return recordProps;
    } catch (Exception e) {
      throw new ActionException(e.getMessage(), e.getCause());
    }
  }

  private int getDesiredInstanceCount(
      AdjustmentType type, Capacity capability, int current) {
    int scalingAdjustment = capability.getAdjustment();
    int desiredCount;
    switch (type) {
    case DELTA_COUNT:
      desiredCount = current + scalingAdjustment;
      break;
    case DELTA_PERCENTAGE:
      desiredCount = current + (int) (ceil(
          current * ((double) scalingAdjustment / MAX_CAPACITY)));
      break;
    case EXACT:
      desiredCount = capability.getAdjustment();
      break;
    default:
      desiredCount = current;
    }
    int minSize = capability.getMin();
    int maxSize = capability.getMax();
    return
        desiredCount < minSize ? minSize
            : desiredCount > maxSize ? maxSize : desiredCount;
  }

  public static int getRequestedNum(SliderApp app, String component) {
    for (Map.Entry<String, SliderAppComponent> e : app.getComponents()
        .entrySet()) {
      if (component.equals(e.getValue().getComponentName())) {
        return e.getValue().getRequestedInstances();
      }
    }
    return -1;
  }

  public static int getAllocatedNum(SliderApp app, String component) {
    for (Map.Entry<String, SliderAppComponent> e : app.getComponents()
        .entrySet()) {
      if (component.equals(e.getValue().getComponentName())) {
        return e.getValue().getActualInstances();
      }
    }
    return -1;
  }
}
