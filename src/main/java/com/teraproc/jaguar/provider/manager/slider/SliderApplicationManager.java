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
import org.slf4j.MDC;

@Service
public class SliderApplicationManager implements ApplicationManager {
  private static final Logger LOGGER =
      JaguarLoggerFactory.getLogger(SliderApplicationManager.class);
  private static final Logger EVENTLOGGER =
      JaguarLoggerFactory.getLogger("EVENT_LOGGER");
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
  public SliderAppState getApplicationState(JaguarUser user, String appName) {
    try {
      SliderApp app = sliderClientProxy.getSliderApp(user, appName);
      return new SliderAppState(app.getState());
    } catch (YarnException | IOException | InterruptedException e) {
      // STATE_ORPHAN
      return new SliderAppState(6);
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
      MDC.put(
          "serviceId",
          context.getProperty(ActionProperties.PROPERTY_APPLICATION_ID));
      EVENTLOGGER.info(policyId, "Trigger action '{}'", jsonDef);

      Properties rsps;
      // scale-up/scale-down instance
      if (scope.equals(Scope.INSTANCE)) {
        String containerId = context.getProperty(PROPERTY_CONTAINER_ID);
        rsps = scaleInstance(
            user, appName, cmptName, containerId, policyId, def);
      } else {
        // scale-out/scale-in application
        rsps = scaleApplication(
            user, appName, def.getComponentName(), policyId, def);
      }
      MDC.remove("serviceId");
      return rsps;
    } catch (Exception e) {
      throw new ActionException(e.getMessage(), e.getCause());
    }
  }

  @Override
  public Provider getProvider() {
    return Provider.SLIDER;
  }

  private Properties scaleInstance(
      JaguarUser user, String appName, String cmptName, String container,
      long policyId, SliderActionDefinition def) throws Exception {
    Properties recordProps = new Properties();
    recordProps.put(ActionProperties.PROPERTY_STATUS, ActionStatus.NONE);
    SliderApp app = sliderClientProxy.getSliderApp(user, appName);

    Map<ResourceType, Integer> allocated = getContainerAllocatedResource(
        app, cmptName, container);
    // Cannot get current resource usage from slider
    if (allocated.size() == 0) {
      EVENTLOGGER.info(
          policyId,
          "No scaling activity due to: cannot get allocated"
              + " resource from slider,"
              + " please check slider command \"slider status '{}'\"", appName);
      return recordProps;
    }

    int allocatedCpu = allocated.get(ResourceType.CPU);
    int allocatedMemory = allocated.get(ResourceType.MEMORY);
    EVENTLOGGER.info(
        policyId,
        "Allocated resource to container '{}': vCore = '{}', memory = '{}'",
        container, allocatedCpu, allocatedMemory);

    // set "desiredCpu = allocatedCpu" if cpu is not scaled
    int desiredCpu = def.getScalingAdjustment().containsKey(ResourceType.CPU)
        ? getDesiredResource(
            def.getAdjustmentType(),
            def.getScalingAdjustment().get(ResourceType.CPU), allocatedCpu)
        : allocatedCpu;
    // set "desiredMemory = allocatedMemory" if memory is not scaled
    int desiredMemory =
        def.getScalingAdjustment().containsKey(ResourceType.MEMORY)
            ? getDesiredResource(
                def.getAdjustmentType(),
                def.getScalingAdjustment().get(ResourceType.MEMORY),
                allocatedMemory) : allocatedMemory;
    EVENTLOGGER.info(
        policyId,
        "Desired resource to container '{}': vCore = '{}', memory = '{}'",
        container, desiredCpu, desiredMemory);

    // resize container only if desired does not equal to allocated
    if (desiredCpu == allocatedCpu && desiredMemory == allocatedMemory) {
      EVENTLOGGER.info(
          policyId,
          "No scaling activity to container '{}' due to: allocated resource "
              + "equals to desired resource",
          container);
      return recordProps;
    }

    // Record manager properties
    recordProps.put(PROPERTY_COMPONENT_NAME, cmptName);
    recordProps.put(PROPERTY_CONTAINER_ID, container);
    recordProps
        .put(PROPERTY_ADJUSTMENT_TYPE, def.getAdjustmentType().toString());
    String adjust = "";
    for (Map.Entry<ResourceType, Capacity> entry : def.getScalingAdjustment()
        .entrySet()) {
      ResourceType res = entry.getKey();
      Capacity cap = entry.getValue();
      adjust = res.toString() + "[" + cap.toString() + "] ";
    }
    recordProps.put(PROPERTY_ADJUSTMENT, adjust);
    recordProps.put(
        PROPERTY_ORIGINAL_ALLOCATED,
        "vCore=" + allocatedCpu + " memory=" + allocatedMemory);
    recordProps.put(
        PROPERTY_DESIRED,
        "vCore=" + desiredCpu + " memory=" + desiredMemory);

    try {
      EVENTLOGGER.info(
          policyId,
          "Sending request to resize application '{}' component '{}' "
              + "container '{}' to: vCore = '{}', memory = '{}'",
          appName, def.getComponentName(), container, desiredCpu,
          desiredMemory);
      sliderClientProxy.resizeContainer(
          user, appName, container, desiredCpu, desiredMemory);
      EVENTLOGGER
          .info(policyId, "Scaling request is successfully sent to slider");
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
      EVENTLOGGER.info(
          policyId,
          "No scaling activity due to: cannot get allocated"
              + " resource from slider,"
              + " please check slider command \"slider status '{}'\"", appName);
      return recordProps;
    }

    EVENTLOGGER.info(
        policyId,
        "Allocated resource to component '{}': allocated = '{}', requested = "
            + "'{}'",
        def.getComponentName(), allocated, requested);
    int desired = getDesiredResource(
        def.getAdjustmentType(),
        def.getScalingAdjustment().get(ResourceType.COUNT), allocated);
    EVENTLOGGER.info(
        policyId,
        "Desired resource to component '{}': instances = '{}'",
        def.getComponentName(), desired);
    // Trigger manager if the desired resource is less than outstanding request
    if (requested != 0 && desired >= (requested + allocated)) {
      EVENTLOGGER.info(
          policyId,
          "No scaling activity due to: outstanding request '{}' has not been "
              + "allocated.",
          requested);
      return recordProps;
    } else if (requested == 0 && desired == (requested + allocated)) {
      EVENTLOGGER.info(
          policyId,
          "No scaling activity to component '{}' due to: allocated resource "
              + "equals to desired resource",
          def.getComponentName());
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
      EVENTLOGGER.info(
          policyId,
          "Sending request to change application '{}' component '{}' to '{}' "
              + "instance(s)",
          appName, def.getComponentName(), desired);
      Map<String, Integer> componentsMap = new HashMap<String, Integer>();
      componentsMap.put(def.getComponentName(), desired);
      sliderClientProxy.flexApp(user, appName, componentsMap);
      EVENTLOGGER
          .info(policyId, "Scaling request is successfully sent to slider");
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

  private int getDesiredResource(
      AdjustmentType type, Capacity capability, int current) {
    int scalingAdjustment = capability.getAdjustment();
    int desired;
    switch (type) {
    case DELTA_COUNT:
      desired = current + scalingAdjustment;
      break;
    case DELTA_PERCENTAGE:
      desired = current + (int) (ceil(
          current * ((double) scalingAdjustment / MAX_CAPACITY)));
      break;
    case EXACT:
      desired = capability.getAdjustment();
      break;
    default:
      desired = current;
    }
    int minSize = capability.getMin();
    int maxSize = capability.getMax();
    return
        desired < minSize ? minSize
            : desired > maxSize ? maxSize : desired;
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
  public static Map<ResourceType, Integer> getContainerAllocatedResource(
      SliderApp app, String component, String containerId) {
    Map<String, Map<ResourceType, Integer>> cmptContainers =
        app.getComponents().get(component).getContainers();
    if (cmptContainers.containsKey(containerId)) {
      return cmptContainers.get(containerId);
    } else {
      return new HashMap<>();
    }
  }
}
