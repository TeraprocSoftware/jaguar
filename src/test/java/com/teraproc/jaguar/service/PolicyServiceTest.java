package com.teraproc.jaguar.service;

import com.teraproc.jaguar.TestUtils;
import com.teraproc.jaguar.domain.Policy;
import com.teraproc.jaguar.domain.Scope;
import com.teraproc.jaguar.domain.JaguarUser;
import com.teraproc.jaguar.domain.Provider;
import com.teraproc.jaguar.provider.manager.ApplicationManager;
import com.teraproc.jaguar.provider.manager.slider.SliderApplicationManager;
import com.teraproc.jaguar.repository.PolicyRepository;
import com.teraproc.jaguar.rest.converter.PolicyConverter;
import com.teraproc.jaguar.rest.json.PolicyJson;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.any;

import java.util.HashMap;
import java.util.Map;

public class PolicyServiceTest {
  @Mock
  private PolicyRepository policyRepository;
  @Mock
  private PolicyConverter policyConverter;
  @Mock
  private ApplicationService applicationService;
  @Mock
  private Map<Provider, ApplicationManager> applicationManagers =
      new HashMap<>();
  @InjectMocks
  private PolicyService underTest = new PolicyService();

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testCreatePolicy() {
    when(applicationService.findOneByUser(any(JaguarUser.class), anyLong()))
        .thenReturn(TestUtils.getApplication());
    when(applicationManagers.get(any(Provider.class))).thenReturn(
        Mockito.mock(SliderApplicationManager.class));
    when(policyRepository.save(any(Policy.class))).thenReturn(
        Mockito.mock(Policy.class));

    JaguarUser user = TestUtils.getJaguarUser();
    Policy policy = TestUtils.getGroupPolicy();
    underTest.createPolicy(user, 1L, policy, Scope.GROUP);
    verify(policyRepository, times(1)).save(any(Policy.class));
  }

  @Test(expected = InvalidFormatException.class)
  public void TestCreatePolicyNoField() {
    when(applicationService.findOneByUser(any(JaguarUser.class), anyLong()))
        .thenReturn(TestUtils.getApplication());
    when(applicationManagers.get(any(Provider.class))).thenReturn(
        Mockito.mock(SliderApplicationManager.class));

    JaguarUser user = TestUtils.getJaguarUser();
    Policy policy = TestUtils.getGroupPolicy();
    // no "successiveIntervals"
    policy.setAlertDefinition(
        "{\"condition\":{\"componentName\":\"HBASE_REGIONSERVER\","
            + "\"evalMethod\":\"AGGREGATE\",\"expression\":\"avg"
            + "(ProcessCallTime_mean)>100\"}}");
    underTest.createPolicy(user, 1L, policy, Scope.GROUP);
  }

  @Test(expected = InvalidFormatException.class)
  public void TestCreatePolicyWithIncorrectFieldName() {
    when(applicationService.findOneByUser(any(JaguarUser.class), anyLong()))
        .thenReturn(TestUtils.getApplication());
    when(applicationManagers.get(any(Provider.class))).thenReturn(
        Mockito.mock(SliderApplicationManager.class));

    JaguarUser user = TestUtils.getJaguarUser();
    Policy policy = TestUtils.getGroupPolicy();
    // "component" should be "componentName"
    policy.setAlertDefinition(
        "{\"successiveIntervals\":2,"
            + "\"condition\":{\"component\":\"HBASE_REGIONSERVER\","
            + "\"evalMethod\":\"AGGREGATE\",\"expression\":\"avg"
            + "(ProcessCallTime_mean)>100\"}}");
    underTest.createPolicy(user, 1L, policy, Scope.GROUP);
  }

  @Test(expected = InvalidFormatException.class)
  public void TestCreatePolicyUnsupportedField() {
    when(applicationService.findOneByUser(any(JaguarUser.class), anyLong()))
        .thenReturn(TestUtils.getApplication());
    when(applicationManagers.get(any(Provider.class))).thenReturn(
        Mockito.mock(SliderApplicationManager.class));

    JaguarUser user = TestUtils.getJaguarUser();
    Policy policy = TestUtils.getGroupPolicy();
    // "evalMethod" is unknown
    policy.setAlertDefinition(
        "{\"successiveIntervals\":2,"
            + "\"condition\":{\"component\":\"HBASE_REGIONSERVER\","
            + "\"evalMethod\":\"UNKNOWN\",\"expression\":\"avg"
            + "(ProcessCallTime_mean)>100\"}}");
    underTest.createPolicy(user, 1L, policy, Scope.GROUP);
  }

  @Test
  public void testUpdatePolicy() {
    when(applicationService.findOneByUser(any(JaguarUser.class), anyLong()))
        .thenReturn(TestUtils.getApplication());
    when(applicationManagers.get(any(Provider.class))).thenReturn(
        Mockito.mock(SliderApplicationManager.class));
    when(policyConverter.update(any(Policy.class), any(PolicyJson.class)))
        .thenReturn(TestUtils.getGroupPolicy());
    when(policyRepository.save(any(Policy.class))).thenReturn(
        Mockito.mock(Policy.class));
    when(policyRepository.findByApplication(anyLong(), anyLong())).thenReturn(
        TestUtils.getGroupPolicy());

    JaguarUser user = TestUtils.getJaguarUser();
    PolicyJson json = TestUtils.getPolicyJson();
    underTest.updatePolicy(user, 1L, 1L, json);
    verify(policyRepository, times(1)).save(any(Policy.class));
  }

  @Test (expected = NotFoundException.class)
  public void testUpdateNotExistingPolicy() {
    when(applicationService.findOneByUser(any(JaguarUser.class), anyLong()))
        .thenReturn(TestUtils.getApplication());
    when(policyRepository.findByApplication(anyLong(), anyLong())).thenReturn(
        null);
    JaguarUser user = TestUtils.getJaguarUser();
    PolicyJson json = TestUtils.getPolicyJson();
    underTest.updatePolicy(user, 1L, 1L, json);
  }

  @Test
  public void testDeletePolicy() {
    when(applicationService.findOneByUser(any(JaguarUser.class), anyLong()))
        .thenReturn(TestUtils.getApplication());
    when(policyRepository.findByApplication(anyLong(), anyLong())).thenReturn(
        Mockito.mock(Policy.class));
    underTest.deletePolicy(TestUtils.getJaguarUser(), 1L, 1L);
    verify(policyRepository, times(1)).delete(any(Policy.class));
  }

  @Test (expected = NotFoundException.class)
  public void testDeleteNotExistingPolicy() {
    when(applicationService.findOneByUser(any(JaguarUser.class), anyLong()))
        .thenReturn(TestUtils.getApplication());
    when(policyRepository.findByApplication(anyLong(), anyLong())).thenReturn(
        null);
    underTest.deletePolicy(TestUtils.getJaguarUser(), 1L, 1L);
  }
}
