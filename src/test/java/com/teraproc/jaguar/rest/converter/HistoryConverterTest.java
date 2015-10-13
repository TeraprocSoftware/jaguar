package com.teraproc.jaguar.rest.converter;

import com.teraproc.jaguar.TestUtils;
import com.teraproc.jaguar.rest.json.HistoryJson;
import org.junit.Before;
import org.junit.Test;

public class HistoryConverterTest extends AbstractConverterTest {
  private HistoryConverter underTest;

  @Before
  public void setup() {
    underTest = new HistoryConverter();
  }

  @Test
  public void testHistoryToJsonTest() throws Exception {
    HistoryJson json = underTest.convert(TestUtils.getHistory());
    assertAllFieldsNotNull(json);
  }
}
