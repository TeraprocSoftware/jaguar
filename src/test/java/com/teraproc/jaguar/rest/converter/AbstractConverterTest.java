package com.teraproc.jaguar.rest.converter;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.Assert.assertFalse;

import com.google.common.collect.ObjectArrays;

public class AbstractConverterTest {
  public void assertAllFieldsNotNull(Object obj) throws Exception {
    Field[] fields = obtainFields(obj);
    for (Field field : fields) {
      assertFieldNotNull(obj, field);
    }
  }

  public void assertAllFieldsNotNull(Object obj, List<String> skippedFields)
      throws Exception {
    Field[] fields = obtainFields(obj);
    for (Field field : fields) {
      if (!skippedFields.contains(field.getName())) {
        assertFieldNotNull(obj, field);
      }
    }
  }

  private void assertFieldNotNull(Object obj, Field field) throws Exception {
    field.setAccessible(true);
    assertFalse(
        "Field '" + field.getName() + "' is null.", field.get(obj) == null);
  }

  private Field[] obtainFields(Object obj) {
    Field[] fields = obj.getClass().getDeclaredFields();
    Field[] parentFields = obj.getClass().getSuperclass().getDeclaredFields();
    return ObjectArrays.concat(fields, parentFields, Field.class);
  }
}