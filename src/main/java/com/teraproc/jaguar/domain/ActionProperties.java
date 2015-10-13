package com.teraproc.jaguar.domain;

/**
 * Action plug-in should record these properties when action is triggered
 */
public final class ActionProperties {
  public static final String PROPERTY_USER_NAME = "user";
  public static final String PROPERTY_POLICY_ID = "policyId";
  public static final String PROPERTY_SCOPE = "scope";
  public static final String PROPERTY_APPLICATION_NAME = "application";
  public static final String PROPERTY_COMPONENT_NAME = "componentName";
  public static final String PROPERTY_CONTAINER_ID = "instance";
  public static final String PROPERTY_APPLICATION_ID = "applicationId";
  public static final String PROPERTY_STATUS = "status";
  public static final String PROPERTY_STATUS_REASON = "status_reason";

  private ActionProperties() {
    //not called
  }
}
