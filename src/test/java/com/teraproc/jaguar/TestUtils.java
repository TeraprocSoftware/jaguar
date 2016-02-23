package com.teraproc.jaguar;

import com.teraproc.jaguar.domain.Policy;
import com.teraproc.jaguar.domain.Application;
import com.teraproc.jaguar.domain.JaguarUser;
import com.teraproc.jaguar.domain.Provider;
import com.teraproc.jaguar.domain.History;
import com.teraproc.jaguar.domain.Scope;
import com.teraproc.jaguar.domain.ActionProperties;
import com.teraproc.jaguar.provider.manager.slider.ResourceType;
import com.teraproc.jaguar.provider.manager.slider.SliderApp;
import com.teraproc.jaguar.provider.manager.slider.SliderAppComponent;
import com.teraproc.jaguar.rest.json.ApplicationJson;
import com.teraproc.jaguar.rest.json.PolicyJson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.HashMap;
import java.util.Arrays;

public class TestUtils {
  public static final String DUMMY_USER = "dummyUser";
  public static final String DUMMY_APP = "hbase";
  public static final String DUMMY_APP_CMPT = "HBASE_REGIONSERVER";
  public static final String DUMMY_CONTAINER = "100";
  public static final String DUMMY_POLICY_SCALE_OUT_NAME =
      "dummyScaleOutPolicy";

  private TestUtils() {
    throw new IllegalStateException();
  }

  public static JaguarUser getJaguarUser() {
    return new JaguarUser(DUMMY_USER);
  }

  public static Application getApplication() {
    Application app = new Application();
    app.setUser(new JaguarUser(DUMMY_USER));
    app.setProvider(Provider.SLIDER);
    app.setEnabled(true);
    app.setState("LIVE");
    app.setId(1L);
    app.setName(DUMMY_APP);
    return app;
  }

  public static List<Application> getApplications(int count) {
    List<Application> apps = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      Application app = new Application();
      app.setUser(new JaguarUser(DUMMY_USER));
      app.setProvider(Provider.SLIDER);
      app.setEnabled(true);
      app.setState("LIVE");
      app.setId(i);
      app.setName(DUMMY_APP + "_" + String.valueOf(i));
      apps.add(app);
    }
    return apps;
  }

  public static ApplicationJson getApplicationJson() {
    ApplicationJson json = new ApplicationJson();
    json.setEnabled(true);
    json.setName(DUMMY_APP);
    json.setProvider(Provider.SLIDER);
    return json;
  }

  public static Policy getGroupPolicy() {
    Policy policy = new Policy();
    policy.setName(DUMMY_POLICY_SCALE_OUT_NAME);
    policy.setId(1L);
    policy.setDescription("Scale-out");
    policy.setApplication(getApplication());
    policy.setEnabled(true);
    policy.setInterval(60);
    policy.setScope(Scope.GROUP);
    policy.setTimeZone("UTC");
    policy.setCron("? * MON-FRI");
    policy.setStartTime("8:00");
    policy.setDuration("10H0M0S");
    policy.setAlertDefinition(
        "{\"successiveIntervals\":2,"
            + "\"condition\":{\"componentName\":\"HBASE_REGIONSERVER\","
            + "\"evalMethod\":\"AGGREGATE\",\"expression\":\"avg"
            + "(ProcessCallTime_mean)>100\"}}");
    policy.setActionDefinition(
        "[{\"componentName\":\"HBASE_REGIONSERVER\","
            + "\"adjustmentType\":\"DELTA_COUNT\","
            + "\"scalingAdjustment\":{\"COUNT\":{\"min\":1,\"max\":3,"
            + "\"adjustment\":1}}}]");
    return policy;
  }

  public static Policy getInstancePolicy() {
    Policy policy = new Policy();
    policy.setName(DUMMY_POLICY_SCALE_OUT_NAME);
    policy.setId(1L);
    policy.setDescription("Scale-up");
    policy.setApplication(getApplication());
    policy.setEnabled(true);
    policy.setInterval(60);
    policy.setScope(Scope.INSTANCE);
    policy.setTimeZone("UTC");
    policy.setCron("? * MON-FRI");
    policy.setStartTime("8:00");
    policy.setDuration("10H0M0S");
    policy.setAlertDefinition(
        "{\"successiveIntervals\":2,"
            + "\"condition\":{\"componentName\":\"HBASE_REGIONSERVER\","
            + "\"expression\":\"avg(ProcessCallTime_mean)>50\"}}");
    policy.setActionDefinition(
        "[{\"componentName\":\"HBASE_REGIONSERVER\","
            + "\"adjustmentType\":\"DELTA_COUNT\","
            + "\"scalingAdjustment\":{\"CPU\":{\"min\":1,\"max\":4,"
            + "\"adjustment\":1}, \"MEMORY\":{\"min\":1024,\"max\":4096,"
            + "\"adjustment\":1024}}}]");
    return policy;
  }

  public static PolicyJson getPolicyJson() {
    PolicyJson json = new PolicyJson();
    json.setName(DUMMY_POLICY_SCALE_OUT_NAME);
    json.setDescription("Scale-out");
    json.setEnabled(true);
    json.setInterval(60);
    json.setTimezone("UTC");
    json.setCron("? * MON-FRI");
    json.setStartTime("8:00");
    json.setDuration("10H0M0S");
    json.setAlert(
        "\"alert\":{\"successiveIntervals\":2, "
            + "\"condition\":{\"componentName\":\"HBASE_REGIONSERVER\","
            + "\"evalMethod\":\"AGGREGATE\",\"expression\":\"avg"
            + "(ProcessCallTime_mean)>100\"}}");
    json.setActions(
        "\"actions\":[\"componentName\":\"HBASE_REGIONSERVER\","
            + "\"adjustmentType\":\"DELTA_COUNT\","
            + "\"scalingAdjustment\":{\"COUNT\":{\"min\":1,\"max\":3,"
            + "\"adjustment\":1}}]");
    return json;
  }

  public static String getScaleAppActionDefinition() {
    return "{\"componentName\":\"HBASE_REGIONSERVER\", \"cooldown\": 60,"
        + "\"adjustmentType\":\"DELTA_COUNT\","
        + "\"scalingAdjustment\":{\"COUNT\":{\"min\":1,\"max\":3,"
        + "\"adjustment\":1}}}";
  }

  public static String getScaleInstanceActionDefinition() {
    return "{\"componentName\":\"HBASE_REGIONSERVER\","
        + "\"adjustmentType\":\"DELTA_COUNT\","
        + "\"scalingAdjustment\":{\"CPU\":{\"min\":1,\"max\":4,"
        + "\"adjustment\":1}, \"MEMORY\":{\"min\":1024,\"max\":4096,"
        + "\"adjustment\":1024}}}";
  }

  public static History getHistory() {
    History history = new History();
    history.setId(1L);
    history.setApplicationId(1L);
    history.setApplication(DUMMY_APP);
    history.setPolicy(DUMMY_POLICY_SCALE_OUT_NAME);
    history.setPolicyId(1L);
    history.setScope(Scope.GROUP);
    history.setTimestamp(System.currentTimeMillis());
    history.setUserId(DUMMY_USER);
    history.setProperties(new HashMap<String, String>());
    return history;
  }

  public static SliderApp getSliderApp() {
    SliderApp app = new SliderApp();
    app.setName(DUMMY_APP);
    app.setType("slider");
    app.setCreateTime(System.currentTimeMillis());
    app.setState(0);
    app.setUpdateTime(System.currentTimeMillis());
    app.setVersion("v1");
    SliderAppComponent cmpt = new SliderAppComponent();
    cmpt.setComponentName(DUMMY_APP_CMPT);
    cmpt.setPriority(1);
    cmpt.setVcores(1);
    cmpt.setMemory(1024);
    cmpt.setActualInstances(1);
    cmpt.setRequestedInstances(0);
    Map<ResourceType, Integer> res = new HashMap<>();
    res.put(ResourceType.CPU, 1);
    res.put(ResourceType.MEMORY, 1024);
    cmpt.getContainers().put(DUMMY_CONTAINER, res);
    app.setComponents(new HashMap<String, SliderAppComponent>());
    app.getComponents().put(DUMMY_APP_CMPT, cmpt);
    return app;
  }

  public static Properties getGroupActionContext() {
    Properties props = new Properties();
    props.put(ActionProperties.PROPERTY_POLICY_ID, "1");
    props.put(ActionProperties.PROPERTY_APPLICATION_NAME, DUMMY_APP);
    props.put(ActionProperties.PROPERTY_COMPONENT_NAME, DUMMY_APP_CMPT);
    props.put(ActionProperties.PROPERTY_SCOPE, Scope.GROUP.toString());
    return props;
  }

  public static Properties getInstanceActionContext() {
    Properties props = new Properties();
    props.put(ActionProperties.PROPERTY_POLICY_ID, "1");
    props.put(ActionProperties.PROPERTY_APPLICATION_NAME, DUMMY_APP);
    props.put(ActionProperties.PROPERTY_COMPONENT_NAME, DUMMY_APP_CMPT);
    props.put(ActionProperties.PROPERTY_SCOPE, Scope.INSTANCE.toString());
    props.put(ActionProperties.PROPERTY_CONTAINER_ID, DUMMY_CONTAINER);
    return props;
  }

  public static Map<String, Map<String, List<Number>>> getInstanceMetrics() {
    // < containerId, < metricName, < metricValues >>>
    Map<String, Map<String, List<Number>>> result = new HashMap<>();

    Map<String, List<Number>> containerMetrics = new HashMap<>();
    containerMetrics.put(
        "ProcessCallTime_mean", Arrays.asList(
            new Number[]{100, 200, 300, 400, 500}));
    containerMetrics.put(
        "ProcessCallTime_num_ops", Arrays.asList(
            new Number[]{1, 2, 3, 4, 5}));
    containerMetrics.put(
        "QueueCallTime_mean", Arrays.asList(
            new Number[]{100, 200, 300, 400, 500}));
    containerMetrics.put(
        "QueueCallTime_num_ops", Arrays.asList(
            new Number[]{1, 2, 3, 4, 5}));
    result.put("container-1", new HashMap<>(containerMetrics));

    containerMetrics.put(
        "ProcessCallTime_mean", Arrays.asList(
            new Number[]{1, 2, 3, 4, 5}));
    containerMetrics.put(
        "ProcessCallTime_num_ops", Arrays.asList(
            new Number[]{1, 2, 3, 4, 5}));
    containerMetrics.put(
        "QueueCallTime_mean", Arrays.asList(
            new Number[]{1, 2, 3, 4, 5}));
    containerMetrics.put(
        "QueueCallTime_num_ops", Arrays.asList(
            new Number[]{1, 2, 3, 4, 5}));
    result.put("container-2", new HashMap<>(containerMetrics));

    return result;
  }

  public static Map<String, Map<String, Number>> getMetricsLatestValue() {
    // < metricName, < containerId, metricValue >>
    Map<String, Map<String, Number>> result = new HashMap<>();
    Map<String, Number> containerMetrics = new HashMap<>();
    containerMetrics.put("container-1", 100);
    containerMetrics.put("container-2", 200);
    result.put("ProcessCallTime_mean", containerMetrics);
    return result;
  }
}
