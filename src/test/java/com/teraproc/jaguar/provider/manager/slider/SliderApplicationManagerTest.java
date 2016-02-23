package com.teraproc.jaguar.provider.manager.slider;

import com.teraproc.jaguar.TestUtils;
import com.teraproc.jaguar.domain.ActionProperties;
import com.teraproc.jaguar.domain.ActionStatus;
import com.teraproc.jaguar.domain.JaguarUser;
import com.teraproc.jaguar.service.NotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Properties;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class SliderApplicationManagerTest {
  @Mock
  private SliderClientProxy sliderClientProxy;
  @InjectMocks
  private SliderApplicationManager underTest;

  @Before
  public void setup() {
    underTest = new SliderApplicationManager();
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testValidateApplication() throws Exception {
    when(sliderClientProxy.appExists(any(JaguarUser.class), anyString()))
        .thenReturn(true);
    when(
        sliderClientProxy.appComptExists(
            any(JaguarUser.class), anyString(), anyString())).thenReturn(true);

    underTest.validateApplication(
        TestUtils.getJaguarUser(), TestUtils.DUMMY_APP);
    underTest.validateApplicationComponent(
        TestUtils.getJaguarUser(), TestUtils.DUMMY_APP,
        TestUtils.DUMMY_APP_CMPT);
  }

  @Test(expected = NotFoundException.class)
  public void testInvalidApplication() throws Exception {
    when(sliderClientProxy.appExists(any(JaguarUser.class), anyString()))
        .thenReturn(false);
    when(
        sliderClientProxy.appComptExists(
            any(JaguarUser.class), anyString(), anyString())).thenReturn(false);

    underTest.validateApplication(
        TestUtils.getJaguarUser(), TestUtils.DUMMY_APP);
    underTest.validateApplicationComponent(
        TestUtils.getJaguarUser(), TestUtils.DUMMY_APP,
        TestUtils.DUMMY_APP_CMPT);
  }

  @Test
  public void testValidateAction() throws Exception {
    when(
        sliderClientProxy
            .appComptExists(any(JaguarUser.class), anyString(), anyString()))
        .thenReturn(true);
    underTest.validateAction(
        TestUtils.getJaguarUser(), TestUtils.DUMMY_APP,
        TestUtils.getScaleAppActionDefinition());
  }

  @Test(expected = Exception.class)
  public void testInvalidAction() throws Exception {
    when(
        sliderClientProxy
            .appComptExists(any(JaguarUser.class), anyString(), anyString()))
        .thenReturn(false);
    underTest.validateAction(
        TestUtils.getJaguarUser(), TestUtils.DUMMY_APP,
        TestUtils.getScaleAppActionDefinition());
  }

  @Test(expected = Exception.class)
  public void testInvalidActionDefinition() throws Exception {
    when(
        sliderClientProxy
            .appComptExists(any(JaguarUser.class), anyString(), anyString()))
        .thenReturn(true);

    // adjustmentType is unknown
    String json = "{\"componentName\":\"HBASE_REGIONSERVER\","
        + "\"adjustmentType\":\"UNKNOWN\","
        + "\"scalingAdjustment\":{\"COUNT\":{\"min\":1,\"max\":3,"
        + "\"adjustment\":1}}}";
    underTest.validateAction(
        TestUtils.getJaguarUser(), TestUtils.DUMMY_APP, json);
  }

  @Test
  public void testPerformActionScaleApp() throws Exception {
    SliderApp app = TestUtils.getSliderApp();
    Properties context = TestUtils.getGroupActionContext();
    String json = TestUtils.getScaleAppActionDefinition();
    when(sliderClientProxy.getSliderApp(any(JaguarUser.class), anyString()))
        .thenReturn(app);

    // scale-out 1 instance
    Properties result =
        underTest.performAction(TestUtils.getJaguarUser(), context, json);
    assertEquals(
        ActionStatus.SUCCESS,
        result.get(ActionProperties.PROPERTY_STATUS));
    assertEquals("instanceCount=2", result.get("desired"));

    // scale-out 2 instance
    json = "{\"componentName\":\"HBASE_REGIONSERVER\", \"cooldown\": 60,"
        + "\"adjustmentType\":\"DELTA_COUNT\","
        + "\"scalingAdjustment\":{\"COUNT\":{\"min\":1,\"max\":3,"
        + "\"adjustment\":2}}}";
    result =
        underTest.performAction(TestUtils.getJaguarUser(), context, json);
    assertEquals(
        ActionStatus.SUCCESS,
        result.get(ActionProperties.PROPERTY_STATUS));
    assertEquals("instanceCount=3", result.get("desired"));

    // scale-out to exact 5 instances
    json = "{\"componentName\":\"HBASE_REGIONSERVER\", \"cooldown\": 60,"
        + "\"adjustmentType\":\"EXACT\","
        + "\"scalingAdjustment\":{\"COUNT\":{\"min\":1,\"max\":10,"
        + "\"adjustment\":5}}}";
    result =
        underTest.performAction(TestUtils.getJaguarUser(), context, json);
    assertEquals(
        ActionStatus.SUCCESS,
        result.get(ActionProperties.PROPERTY_STATUS));
    assertEquals("instanceCount=5", result.get("desired"));

    // scale-out 50% of existing instances
    app.getComponents().get(TestUtils.DUMMY_APP_CMPT).setActualInstances(4);
    json = "{\"componentName\":\"HBASE_REGIONSERVER\", \"cooldown\": 60,"
        + "\"adjustmentType\":\"DELTA_PERCENTAGE\","
        + "\"scalingAdjustment\":{\"COUNT\":{\"min\":1,\"max\":10,"
        + "\"adjustment\":50}}}";
    result =
        underTest.performAction(TestUtils.getJaguarUser(), context, json);
    assertEquals(
        ActionStatus.SUCCESS,
        result.get(ActionProperties.PROPERTY_STATUS));
    assertEquals("instanceCount=6", result.get("desired"));

    // scale-in 2 instances
    app.getComponents().get(TestUtils.DUMMY_APP_CMPT).setActualInstances(5);
    json = "{\"componentName\":\"HBASE_REGIONSERVER\", \"cooldown\": 60,"
        + "\"adjustmentType\":\"DELTA_COUNT\","
        + "\"scalingAdjustment\":{\"COUNT\":{\"min\":1,\"max\":10,"
        + "\"adjustment\":-2}}}";
    result =
        underTest.performAction(TestUtils.getJaguarUser(), context, json);
    assertEquals(
        ActionStatus.SUCCESS,
        result.get(ActionProperties.PROPERTY_STATUS));
    assertEquals("instanceCount=3", result.get("desired"));

    // scale-in to exact 5 instances
    app.getComponents().get(TestUtils.DUMMY_APP_CMPT).setActualInstances(8);
    json = "{\"componentName\":\"HBASE_REGIONSERVER\", \"cooldown\": 60,"
        + "\"adjustmentType\":\"EXACT\","
        + "\"scalingAdjustment\":{\"COUNT\":{\"min\":1,\"max\":10,"
        + "\"adjustment\":5}}}";
    result =
        underTest.performAction(TestUtils.getJaguarUser(), context, json);
    assertEquals(
        ActionStatus.SUCCESS,
        result.get(ActionProperties.PROPERTY_STATUS));
    assertEquals("instanceCount=5", result.get("desired"));

    // scale-in 50% of existing instances
    app.getComponents().get(TestUtils.DUMMY_APP_CMPT).setActualInstances(8);
    json = "{\"componentName\":\"HBASE_REGIONSERVER\", \"cooldown\": 60,"
        + "\"adjustmentType\":\"DELTA_PERCENTAGE\","
        + "\"scalingAdjustment\":{\"COUNT\":{\"min\":1,\"max\":10,"
        + "\"adjustment\":-50}}}";
    result =
        underTest.performAction(TestUtils.getJaguarUser(), context, json);
    assertEquals(
        ActionStatus.SUCCESS,
        result.get(ActionProperties.PROPERTY_STATUS));
    assertEquals("instanceCount=4", result.get("desired"));

    // scale-out round-up to max
    app.getComponents().get(TestUtils.DUMMY_APP_CMPT).setActualInstances(9);
    json = "{\"componentName\":\"HBASE_REGIONSERVER\", \"cooldown\": 60,"
        + "\"adjustmentType\":\"DELTA_COUNT\","
        + "\"scalingAdjustment\":{\"COUNT\":{\"min\":1,\"max\":10,"
        + "\"adjustment\":2}}}";
    result =
        underTest.performAction(TestUtils.getJaguarUser(), context, json);
    assertEquals(
        ActionStatus.SUCCESS,
        result.get(ActionProperties.PROPERTY_STATUS));
    assertEquals("instanceCount=10", result.get("desired"));

    // scale-in round-up to min
    app.getComponents().get(TestUtils.DUMMY_APP_CMPT).setActualInstances(8);
    json = "{\"componentName\":\"HBASE_REGIONSERVER\", \"cooldown\": 60,"
        + "\"adjustmentType\":\"DELTA_PERCENTAGE\","
        + "\"scalingAdjustment\":{\"COUNT\":{\"min\":5,\"max\":10,"
        + "\"adjustment\":-50}}}";
    result =
        underTest.performAction(TestUtils.getJaguarUser(), context, json);
    assertEquals(
        ActionStatus.SUCCESS,
        result.get(ActionProperties.PROPERTY_STATUS));
    assertEquals("instanceCount=5", result.get("desired"));

    // has outstanding request, no action
    app.getComponents().get(TestUtils.DUMMY_APP_CMPT).setActualInstances(5);
    app.getComponents().get(TestUtils.DUMMY_APP_CMPT).setRequestedInstances(1);
    json = "{\"componentName\":\"HBASE_REGIONSERVER\", \"cooldown\": 60,"
        + "\"adjustmentType\":\"DELTA_COUNT\","
        + "\"scalingAdjustment\":{\"COUNT\":{\"min\":1,\"max\":10,"
        + "\"adjustment\":2}}}";
    result =
        underTest.performAction(TestUtils.getJaguarUser(), context, json);
    assertEquals(
        ActionStatus.NONE,
        result.get(ActionProperties.PROPERTY_STATUS));

    // has outstanding request, but scale-out if desired is less than
    // outstanding demand. You still have a chance to get more resource
    app.getComponents().get(TestUtils.DUMMY_APP_CMPT).setActualInstances(5);
    app.getComponents().get(TestUtils.DUMMY_APP_CMPT).setRequestedInstances(2);
    json = "{\"componentName\":\"HBASE_REGIONSERVER\", \"cooldown\": 60,"
        + "\"adjustmentType\":\"DELTA_COUNT\","
        + "\"scalingAdjustment\":{\"COUNT\":{\"min\":1,\"max\":10,"
        + "\"adjustment\":1}}}";
    result =
        underTest.performAction(TestUtils.getJaguarUser(), context, json);
    assertEquals(
        ActionStatus.SUCCESS,
        result.get(ActionProperties.PROPERTY_STATUS));
    assertEquals("instanceCount=6", result.get("desired"));
  }

  @Test
  public void testPerformActionScaleInstance() throws Exception {
    SliderApp app = TestUtils.getSliderApp();
    Properties context = TestUtils.getInstanceActionContext();
    String json = TestUtils.getScaleInstanceActionDefinition();
    when(sliderClientProxy.getSliderApp(any(JaguarUser.class), anyString()))
        .thenReturn(app);

    // scale-up 1 cpu and 1024 memory
    Properties result =
        underTest.performAction(TestUtils.getJaguarUser(), context, json);
    assertEquals(
        ActionStatus.SUCCESS,
        result.get(ActionProperties.PROPERTY_STATUS));
    assertEquals("vCore=2 memory=2048", result.get("desired"));

    // scale-up 2 cpu and 2048 memory
    json = "{\"componentName\":\"HBASE_REGIONSERVER\","
        + "\"adjustmentType\":\"DELTA_COUNT\","
        + "\"scalingAdjustment\":{\"CPU\":{\"min\":1,\"max\":4,"
        + "\"adjustment\":2}, \"MEMORY\":{\"min\":1024,\"max\":4096,"
        + "\"adjustment\":2048}}}";
    result =
        underTest.performAction(TestUtils.getJaguarUser(), context, json);
    assertEquals(
        ActionStatus.SUCCESS,
        result.get(ActionProperties.PROPERTY_STATUS));
    assertEquals("vCore=3 memory=3072", result.get("desired"));

    // scale-up to exact 4 cpu and 10240 memory
    json = "{\"componentName\":\"HBASE_REGIONSERVER\", \"cooldown\": 60,"
        + "\"adjustmentType\":\"EXACT\","
        + "\"scalingAdjustment\":{\"CPU\":{\"min\":1,\"max\":10,"
        + "\"adjustment\":4}, \"MEMORY\":{\"min\":1,\"max\":10240,"
        + "\"adjustment\":10240}}}";
    result =
        underTest.performAction(TestUtils.getJaguarUser(), context, json);
    assertEquals(
        ActionStatus.SUCCESS, result.get(ActionProperties.PROPERTY_STATUS));
    assertEquals("vCore=4 memory=10240", result.get("desired"));

    // scale-up 50% of existing resources
    app.getComponents().get(TestUtils.DUMMY_APP_CMPT).getContainers()
        .get(TestUtils.DUMMY_CONTAINER).put(ResourceType.CPU, 4);
    app.getComponents().get(TestUtils.DUMMY_APP_CMPT).getContainers().get(
        TestUtils.DUMMY_CONTAINER).put(ResourceType.MEMORY, 4096);
    json = "{\"componentName\":\"HBASE_REGIONSERVER\", \"cooldown\": 60,"
        + "\"adjustmentType\":\"DELTA_PERCENTAGE\","
        + "\"scalingAdjustment\":{\"CPU\":{\"min\":1,\"max\":10,"
        + "\"adjustment\":50}, \"MEMORY\":{\"min\":1,\"max\":10240,"
        + "\"adjustment\":50}}}";
    result =
        underTest.performAction(TestUtils.getJaguarUser(), context, json);
    assertEquals(
        ActionStatus.SUCCESS,
        result.get(ActionProperties.PROPERTY_STATUS));
    assertEquals("vCore=6 memory=6144", result.get("desired"));

    // scale-up cpu only
    app.getComponents().get(TestUtils.DUMMY_APP_CMPT).getContainers()
        .get(TestUtils.DUMMY_CONTAINER).put(ResourceType.CPU, 1);
    app.getComponents().get(TestUtils.DUMMY_APP_CMPT).getContainers().get(
        TestUtils.DUMMY_CONTAINER).put(ResourceType.MEMORY, 1024);
    json = "{\"componentName\":\"HBASE_REGIONSERVER\","
        + "\"adjustmentType\":\"DELTA_COUNT\","
        + "\"scalingAdjustment\":{\"CPU\":{\"min\":1,\"max\":4,"
        + "\"adjustment\":2}}}";
    result =
        underTest.performAction(TestUtils.getJaguarUser(), context, json);
    assertEquals(
        ActionStatus.SUCCESS,
        result.get(ActionProperties.PROPERTY_STATUS));
    assertEquals("vCore=3 memory=1024", result.get("desired"));

    // scale-up memory only
    json = "{\"componentName\":\"HBASE_REGIONSERVER\","
        + "\"adjustmentType\":\"DELTA_COUNT\","
        + "\"scalingAdjustment\":{\"MEMORY\":{\"min\":1024,\"max\":4096,"
        + "\"adjustment\":2048}}}";
    result =
        underTest.performAction(TestUtils.getJaguarUser(), context, json);
    assertEquals(
        ActionStatus.SUCCESS,
        result.get(ActionProperties.PROPERTY_STATUS));
    assertEquals("vCore=1 memory=3072", result.get("desired"));

    // scale-down 1 cpu 2048 memory
    app.getComponents().get(TestUtils.DUMMY_APP_CMPT).getContainers()
        .get(TestUtils.DUMMY_CONTAINER).put(ResourceType.CPU, 4);
    app.getComponents().get(TestUtils.DUMMY_APP_CMPT).getContainers().get(
        TestUtils.DUMMY_CONTAINER).put(ResourceType.MEMORY, 4096);
    json = "{\"componentName\":\"HBASE_REGIONSERVER\","
        + "\"adjustmentType\":\"DELTA_COUNT\","
        + "\"scalingAdjustment\":{\"CPU\":{\"min\":1,\"max\":4,"
        + "\"adjustment\":-1}, \"MEMORY\":{\"min\":1024,\"max\":4096,"
        + "\"adjustment\":-2048}}}";
    result =
        underTest.performAction(TestUtils.getJaguarUser(), context, json);
    assertEquals(
        ActionStatus.SUCCESS,
        result.get(ActionProperties.PROPERTY_STATUS));
    assertEquals("vCore=3 memory=2048", result.get("desired"));

    // scale-down to exact 1 cpu and 2048 memory
    json = "{\"componentName\":\"HBASE_REGIONSERVER\", \"cooldown\": 60,"
        + "\"adjustmentType\":\"EXACT\","
        + "\"scalingAdjustment\":{\"CPU\":{\"min\":1,\"max\":10,"
        + "\"adjustment\":1}, \"MEMORY\":{\"min\":1,\"max\":10240,"
        + "\"adjustment\":2048}}}";
    result =
        underTest.performAction(TestUtils.getJaguarUser(), context, json);
    assertEquals(
        ActionStatus.SUCCESS, result.get(ActionProperties.PROPERTY_STATUS));
    assertEquals("vCore=1 memory=2048", result.get("desired"));

    // scale-down 50% of existing resources
    json = "{\"componentName\":\"HBASE_REGIONSERVER\", \"cooldown\": 60,"
        + "\"adjustmentType\":\"DELTA_PERCENTAGE\","
        + "\"scalingAdjustment\":{\"CPU\":{\"min\":1,\"max\":10,"
        + "\"adjustment\":-50}, \"MEMORY\":{\"min\":1,\"max\":10240,"
        + "\"adjustment\":-50}}}";
    result =
        underTest.performAction(TestUtils.getJaguarUser(), context, json);
    assertEquals(
        ActionStatus.SUCCESS,
        result.get(ActionProperties.PROPERTY_STATUS));
    assertEquals("vCore=2 memory=2048", result.get("desired"));

    // scale-down cpu only
    app.getComponents().get(TestUtils.DUMMY_APP_CMPT).getContainers()
        .get(TestUtils.DUMMY_CONTAINER).put(ResourceType.CPU, 4);
    app.getComponents().get(TestUtils.DUMMY_APP_CMPT).getContainers().get(
        TestUtils.DUMMY_CONTAINER).put(ResourceType.MEMORY, 4096);
    json = "{\"componentName\":\"HBASE_REGIONSERVER\","
        + "\"adjustmentType\":\"DELTA_COUNT\","
        + "\"scalingAdjustment\":{\"CPU\":{\"min\":1,\"max\":4,"
        + "\"adjustment\":-3}}}";
    result =
        underTest.performAction(TestUtils.getJaguarUser(), context, json);
    assertEquals(
        ActionStatus.SUCCESS,
        result.get(ActionProperties.PROPERTY_STATUS));
    assertEquals("vCore=1 memory=4096", result.get("desired"));

    // scale-down memory only
    json = "{\"componentName\":\"HBASE_REGIONSERVER\","
        + "\"adjustmentType\":\"DELTA_COUNT\","
        + "\"scalingAdjustment\":{\"MEMORY\":{\"min\":1024,\"max\":4096,"
        + "\"adjustment\":-2048}}}";
    result =
        underTest.performAction(TestUtils.getJaguarUser(), context, json);
    assertEquals(
        ActionStatus.SUCCESS,
        result.get(ActionProperties.PROPERTY_STATUS));
    assertEquals("vCore=4 memory=2048", result.get("desired"));

    // scale-up round-up to max
    json = "{\"componentName\":\"HBASE_REGIONSERVER\","
        + "\"adjustmentType\":\"DELTA_COUNT\","
        + "\"scalingAdjustment\":{\"CPU\":{\"min\":1,\"max\":5,"
        + "\"adjustment\":2}, \"MEMORY\":{\"min\":1024,\"max\":5120,"
        + "\"adjustment\":2048}}}";
    result =
        underTest.performAction(TestUtils.getJaguarUser(), context, json);
    assertEquals(
        ActionStatus.SUCCESS,
        result.get(ActionProperties.PROPERTY_STATUS));
    assertEquals("vCore=5 memory=5120", result.get("desired"));

    // scale-down round-up to min
    json = "{\"componentName\":\"HBASE_REGIONSERVER\","
        + "\"adjustmentType\":\"DELTA_COUNT\","
        + "\"scalingAdjustment\":{\"CPU\":{\"min\":1,\"max\":5,"
        + "\"adjustment\":-10}, \"MEMORY\":{\"min\":1024,\"max\":5120,"
        + "\"adjustment\":-10240}}}";
    result =
        underTest.performAction(TestUtils.getJaguarUser(), context, json);
    assertEquals(
        ActionStatus.SUCCESS,
        result.get(ActionProperties.PROPERTY_STATUS));
    assertEquals("vCore=1 memory=1024", result.get("desired"));

    // no action if required equals to allocated
    json = "{\"componentName\":\"HBASE_REGIONSERVER\", \"cooldown\": 60,"
        + "\"adjustmentType\":\"EXACT\","
        + "\"scalingAdjustment\":{\"CPU\":{\"min\":1,\"max\":10,"
        + "\"adjustment\":4}, \"MEMORY\":{\"min\":1,\"max\":10240,"
        + "\"adjustment\":4096}}}";
    result =
        underTest.performAction(TestUtils.getJaguarUser(), context, json);
    assertEquals(
        ActionStatus.NONE,
        result.get(ActionProperties.PROPERTY_STATUS));
  }

  @Test
  public void testPerformActionSliderUnavailable() throws Exception {
    SliderApp app = TestUtils.getSliderApp();
    SliderAppComponent cmpt = app.getComponents().get(TestUtils.DUMMY_APP_CMPT);
    cmpt.setComponentName("unknown");
    app.getComponents().remove(TestUtils.DUMMY_APP_CMPT);
    app.getComponents().put("unknown", cmpt);
    when(sliderClientProxy.getSliderApp(any(JaguarUser.class), anyString()))
        .thenReturn(app);

    Properties result = underTest.performAction(
        TestUtils.getJaguarUser(), TestUtils.getGroupActionContext(),
        TestUtils.getScaleAppActionDefinition());
    assertEquals(
        ActionStatus.NONE,
        result.get(ActionProperties.PROPERTY_STATUS));
  }
}
