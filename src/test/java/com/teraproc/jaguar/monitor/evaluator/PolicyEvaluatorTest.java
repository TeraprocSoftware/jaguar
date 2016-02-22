package com.teraproc.jaguar.monitor.evaluator;

import com.teraproc.jaguar.TestUtils;
import com.teraproc.jaguar.domain.Policy;
import com.teraproc.jaguar.domain.Application;
import com.teraproc.jaguar.domain.InternalPolicy;
import com.teraproc.jaguar.domain.Provider;
import com.teraproc.jaguar.domain.GroupAlert;
import com.teraproc.jaguar.domain.InstanceAlert;
import com.teraproc.jaguar.monitor.event.ScalingEvent;
import com.teraproc.jaguar.provider.manager.ApplicationManager;
import com.teraproc.jaguar.provider.manager.slider.SliderApplicationManager;
import com.teraproc.jaguar.provider.metrics.ElasticsearchClientProvider;
import com.teraproc.jaguar.repository.PolicyRepository;
import com.teraproc.jaguar.rest.converter.PolicyConverter;
import com.teraproc.jaguar.service.ApplicationService;
import com.teraproc.jaguar.service.PolicyService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.never;

public class PolicyEvaluatorTest {
  @Mock
  private ApplicationService applicationService;
  @Mock
  private PolicyService policyService;
  @Mock
  private ElasticsearchClientProvider elasticsearchClient;
  @Mock
  private ApplicationEventPublisher eventPublisher;
  @InjectMocks
  private PolicyEvaluator underTest = new PolicyEvaluator();
  @Mock
  private PolicyRepository policyRepository;
  @Mock
  private PolicyConverter policyConverter;
  @Mock
  private Map<Provider, ApplicationManager> applicationManagers =
      new HashMap<>();

  @InjectMocks
  private PolicyService policyParser = new PolicyService();

  private Policy policy;
  private InternalPolicy internal;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    when(applicationManagers.get(any(Provider.class))).thenReturn(
        Mockito.mock(SliderApplicationManager.class));
  }

  @Test
  public void TestEvaluateGroupAlert() throws Exception {
    when(
        elasticsearchClient.getMetricsLatestValue(
            anyString(), anyString(), anyListOf(
                String.class), anyLong(), anyLong()))
        .thenReturn(TestUtils.getMetricsLatestValue());
    when(
        elasticsearchClient.getInstanceMetrics(
            anyString(), anyString(), anyListOf(
                String.class), anyLong(), anyLong()))
        .thenReturn(TestUtils.getInstanceMetrics());
    when(applicationService.find(anyLong())).thenReturn(
        Mockito.mock(
            Application.class));
    policy = TestUtils.getGroupPolicy();
    policy.setAlertDefinition(
        "{\"successiveIntervals\":2,"
            + "\"condition\":{\"componentName\":\"HBASE_REGIONSERVER\","
            + "\"evalMethod\":\"AGGREGATE\",\"expression\":\"avg"
            + "(ProcessCallTime_mean)>1\"}}");
    internal = policyParser.parsePolicy(policy);
    assertEquals(
        true, underTest.evaluateGroupAlert((GroupAlert) internal.getAlert()));

    policy.setAlertDefinition(
        "{\"successiveIntervals\":2,"
            + "\"condition\":{\"componentName\":\"HBASE_REGIONSERVER\","
            + "\"evalMethod\":\"AGGREGATE\",\"expression\":\"avg"
            + "(ProcessCallTime_mean)>1000\"}}");
    internal = policyParser.parsePolicy(policy);
    assertEquals(
        false, underTest.evaluateGroupAlert((GroupAlert) internal.getAlert()));

    policy.setAlertDefinition(
        "{\"successiveIntervals\":2,"
            + "\"condition\":{\"componentName\":\"HBASE_REGIONSERVER\","
            + "\"evalMethod\":\"PERCENT\", \"threshold\":\">60\", "
            + "\"expression\":\"avg"
            + "(ProcessCallTime_mean)>1\"}}");
    internal = policyParser.parsePolicy(policy);
    assertEquals(
        true, underTest.evaluateGroupAlert((GroupAlert) internal.getAlert()));

    policy.setAlertDefinition(
        "{\"successiveIntervals\":2,"
            + "\"condition\":{\"componentName\":\"HBASE_REGIONSERVER\","
            + "\"evalMethod\":\"PERCENT\", \"threshold\":\">60\", "
            + "\"expression\":\"avg"
            + "(ProcessCallTime_mean)>150\"}}");
    internal = policyParser.parsePolicy(policy);
    assertEquals(
        false, underTest.evaluateGroupAlert((GroupAlert) internal.getAlert()));
  }

  @Test
  public void testEvaluateGroupPolicy() throws Exception {
    when(
        elasticsearchClient.getMetricsLatestValue(
            anyString(), anyString(), anyListOf(
                String.class), anyLong(), anyLong()))
        .thenReturn(TestUtils.getMetricsLatestValue());
    when(
        elasticsearchClient.getInstanceMetrics(
            anyString(), anyString(), anyListOf(
                String.class), anyLong(), anyLong()))
        .thenReturn(TestUtils.getInstanceMetrics());
    when(applicationService.find(anyLong())).thenReturn(
        Mockito.mock(
            Application.class));
    policy = TestUtils.getGroupPolicy();
    policy.setAlertDefinition(
        "{\"successiveIntervals\":2,"
            + "\"condition\":{\"componentName\":\"HBASE_REGIONSERVER\","
            + "\"evalMethod\":\"AGGREGATE\",\"expression\":\"avg"
            + "(ProcessCallTime_mean)>1\"}}");
    internal = policyParser.parsePolicy(policy);
    ((GroupAlert) internal.getAlert()).setLatestSuccessiveIntervals(1);
    underTest.evaluateGroupPolicy(internal);
    verify(eventPublisher, times(1)).publishEvent(any(ScalingEvent.class));
    reset(eventPublisher);

    ((GroupAlert) internal.getAlert()).setLatestSuccessiveIntervals(0);
    underTest.evaluateGroupPolicy(internal);
    verify(eventPublisher, never()).publishEvent(any(ScalingEvent.class));
    assertEquals(
        1, ((GroupAlert) internal.getAlert()).getLatestSuccessiveIntervals());
    reset(eventPublisher);

    policy.setAlertDefinition(
        "{\"successiveIntervals\":2,"
            + "\"condition\":{\"componentName\":\"HBASE_REGIONSERVER\","
            + "\"evalMethod\":\"AGGREGATE\",\"expression\":\"avg"
            + "(ProcessCallTime_mean)>1000\"}}");
    internal = policyParser.parsePolicy(policy);
    ((GroupAlert) internal.getAlert()).setLatestSuccessiveIntervals(3);
    underTest.evaluateGroupPolicy(internal);
    verify(eventPublisher, never()).publishEvent(any(ScalingEvent.class));
    assertEquals(
        0,
        ((GroupAlert) internal.getAlert()).getLatestSuccessiveIntervals());
  }

  @Test
  public void testEvaluateInstanceAlert() throws Exception {
    when(
        elasticsearchClient.getMetricsLatestValue(
            anyString(), anyString(), anyListOf(
                String.class), anyLong(), anyLong()))
        .thenReturn(TestUtils.getMetricsLatestValue());
    when(
        elasticsearchClient.getInstanceMetrics(
            anyString(), anyString(), anyListOf(
                String.class), anyLong(), anyLong()))
        .thenReturn(TestUtils.getInstanceMetrics());
    when(applicationService.find(anyLong())).thenReturn(
        Mockito.mock(
            Application.class));
    policy = TestUtils.getInstancePolicy();
    internal = policyParser.parsePolicy(policy);
    assertEquals(
        1, underTest.evaluateInstanceAlert(
            (InstanceAlert) internal.getAlert()).size());

    policy.setAlertDefinition(
        "{\"successiveIntervals\":2,"
            + "\"condition\":{\"componentName\":\"HBASE_REGIONSERVER\","
            + "\"expression\":\"avg(ProcessCallTime_mean)>50 && "
            + "ProcessCallTime_num_ops>3\"}}");
    internal = policyParser.parsePolicy(policy);
    assertEquals(
        1, underTest.evaluateInstanceAlert(
            (InstanceAlert) internal.getAlert()).size());

    policy.setAlertDefinition(
        "{\"successiveIntervals\":2,"
            + "\"condition\":{\"componentName\":\"HBASE_REGIONSERVER\","
            + "\"expression\":\"avg(ProcessCallTime_mean)>50 || "
            + "ProcessCallTime_num_ops>3\"}}");
    internal = policyParser.parsePolicy(policy);
    assertEquals(
        2, underTest.evaluateInstanceAlert(
            (InstanceAlert) internal.getAlert()).size());
  }

  @Test
  public void testEvaluateInstancePolicy() throws Exception {
    when(
        elasticsearchClient.getMetricsLatestValue(
            anyString(), anyString(), anyListOf(
                String.class), anyLong(), anyLong()))
        .thenReturn(TestUtils.getMetricsLatestValue());
    when(
        elasticsearchClient.getInstanceMetrics(
            anyString(), anyString(), anyListOf(
                String.class), anyLong(), anyLong()))
        .thenReturn(TestUtils.getInstanceMetrics());
    when(applicationService.find(anyLong())).thenReturn(
        Mockito.mock(
            Application.class));
    policy = TestUtils.getInstancePolicy();
    internal = policyParser.parsePolicy(policy);
    Map<String, Integer> history = new HashMap<>();
    history.put("container-1", 1);
    history.put("container-2", 1);
    ((InstanceAlert) internal.getAlert()).setLatestSuccessiveIntervals(history);
    underTest.evaluateInstancePolicy(internal);
    verify(eventPublisher, times(1)).publishEvent(any(ScalingEvent.class));
    assertEquals(
        (Integer) 2,
        ((InstanceAlert) internal.getAlert()).getLatestSuccessiveIntervals()
            .get("container-1"));
    assertEquals(
        null,
        ((InstanceAlert) internal.getAlert()).getLatestSuccessiveIntervals()
            .get("container-2"));
    reset(eventPublisher);

    policy.setAlertDefinition(
        "{\"successiveIntervals\":2,"
            + "\"condition\":{\"componentName\":\"HBASE_REGIONSERVER\","
            + "\"expression\":\"avg(ProcessCallTime_mean)>5000\"}}");
    internal = policyParser.parsePolicy(policy);
    history.clear();
    history.put("container-1", 1);
    history.put("container-2", 1);
    ((InstanceAlert) internal.getAlert()).setLatestSuccessiveIntervals(history);
    underTest.evaluateInstancePolicy(internal);
    verify(eventPublisher, never()).publishEvent(any(ScalingEvent.class));
    assertEquals(
        null,
        ((InstanceAlert) internal.getAlert()).getLatestSuccessiveIntervals()
            .get("container-1"));
    assertEquals(
        null,
        ((InstanceAlert) internal.getAlert()).getLatestSuccessiveIntervals()
            .get("container-2"));
    reset(eventPublisher);

    history.clear();
    history.put("container-2", 1);
    ((InstanceAlert) internal.getAlert()).setLatestSuccessiveIntervals(history);
    underTest.evaluateInstancePolicy(internal);
    verify(eventPublisher, never()).publishEvent(any(ScalingEvent.class));
    assertEquals(
        null,
        ((InstanceAlert) internal.getAlert()).getLatestSuccessiveIntervals()
            .get("container-1"));
    assertEquals(
        null,
        ((InstanceAlert) internal.getAlert()).getLatestSuccessiveIntervals()
            .get("container-2"));
    reset(eventPublisher);
  }
}
