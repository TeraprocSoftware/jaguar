package com.teraproc.jaguar.rest.converter;

import com.teraproc.jaguar.TestUtils;
import com.teraproc.jaguar.domain.Policy;
import com.teraproc.jaguar.rest.json.PolicyJson;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class PolicyConverterTest extends AbstractConverterTest {
  private PolicyConverter underTest;
  private List<String> defaultSkippedFields =
      Arrays.asList("id", "application", "scope");

  @Before
  public void setup() {
    underTest = new PolicyConverter();
  }

  @Test
  public void testPolicyToJsonConverter() throws Exception {
    PolicyJson result = underTest.convert(TestUtils.getGroupPolicy());
    assertAllFieldsNotNull(result);
  }

  @Test
  public void testJsonToPolicyConverter() throws Exception {
    Policy result = underTest.convert(TestUtils.getPolicyJson());
    assertAllFieldsNotNull(result, defaultSkippedFields);
  }

  @Test
  public void testUpdateConverer() throws Exception {
    Policy policy = TestUtils.getGroupPolicy();
    PolicyJson json = TestUtils.getPolicyJson();
    json.setCron("? * MON-SUN");
    Policy result = underTest.update(policy, json);
    assertAllFieldsNotNull(result);
    assertEquals(result.getId(), policy.getId());
    assertEquals(result.getStartTime(), policy.getStartTime());
    assertEquals("? * MON-SUN", result.getCron());
  }
}
