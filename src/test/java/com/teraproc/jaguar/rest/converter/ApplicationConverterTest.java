package com.teraproc.jaguar.rest.converter;


import com.teraproc.jaguar.TestUtils;
import com.teraproc.jaguar.domain.Application;
import com.teraproc.jaguar.rest.json.ApplicationJson;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

public class ApplicationConverterTest extends AbstractConverterTest {
  private List<String> defaultSkippedFields =
      Arrays.asList("id", "user", "state");
  private ApplicationConverter underTest;

  @Before
  public void setup() {
    underTest = new ApplicationConverter();
  }

  @Test
  public void testAppToJsonConverter() throws Exception {
    ApplicationJson result = underTest.convert(TestUtils.getApplication());
    assertAllFieldsNotNull(result);
  }

  @Test
  public void testJsonToAppConverter() throws Exception {
    Application result = underTest.convert(TestUtils.getApplicationJson());
    assertAllFieldsNotNull(result, defaultSkippedFields);
  }

  @Test
  public void testUpdateConverter() throws Exception {
    Application app = TestUtils.getApplication();
    ApplicationJson json = TestUtils.getApplicationJson();
    json.setEnabled(false);
    Application result = underTest.update(app, json);
    assertAllFieldsNotNull(result);
    assertEquals(false, result.getEnabled());
    assertEquals(result.getId(), app.getId());
    assertEquals(result.getName(), app.getName());
    assertEquals(result.getProvider(), app.getProvider());
  }
}
