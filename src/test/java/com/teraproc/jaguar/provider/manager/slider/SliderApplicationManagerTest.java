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
        TestUtils.getActionDefinition());
  }

  @Test(expected = Exception.class)
  public void testInvalidAction() throws Exception {
    when(
        sliderClientProxy
            .appComptExists(any(JaguarUser.class), anyString(), anyString()))
        .thenReturn(false);
    underTest.validateAction(
        TestUtils.getJaguarUser(), TestUtils.DUMMY_APP,
        TestUtils.getActionDefinition());
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
  public void testPerformAction() throws Exception {
    SliderApp app = TestUtils.getSliderApp();
    Properties context = TestUtils.getgActionContext();
    String json = TestUtils.getActionDefinition();
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
  public void testPerformActionSliderUnavailable() throws Exception {
    SliderApp app = TestUtils.getSliderApp();
    SliderAppComponent cmpt = app.getComponents().get(TestUtils.DUMMY_APP_CMPT);
    cmpt.setComponentName("unknown");
    app.getComponents().remove(TestUtils.DUMMY_APP_CMPT);
    app.getComponents().put("unknown", cmpt);
    when(sliderClientProxy.getSliderApp(any(JaguarUser.class), anyString()))
        .thenReturn(app);

    Properties result = underTest.performAction(
        TestUtils.getJaguarUser(), TestUtils.getgActionContext(),
        TestUtils.getActionDefinition());
    assertEquals(
        ActionStatus.NONE,
        result.get(ActionProperties.PROPERTY_STATUS));
  }
}
