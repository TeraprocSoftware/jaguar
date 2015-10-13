package com.teraproc.jaguar.provider.manager;

import com.teraproc.jaguar.domain.JaguarUser;
import com.teraproc.jaguar.domain.Provider;
import com.teraproc.jaguar.service.InvalidFormatException;
import com.teraproc.jaguar.service.NotFoundException;

import java.util.Properties;

/**
 * This interface is used by the PolicyService and ScalingRequest to handle any
 * action with the backend application manager, e.g., slider.
 *
 * Implementation of this interface is required to be thread-safe.
 */
public interface ApplicationManager {

  /**
   * Validate an application with the application manager
   *
   * @param user    the user who performs the validation
   * @param appName the name of the application
   * @throws NotFoundException when the application cannot be found in the
   *                           application manager
   */
  void validateApplication(JaguarUser user, String appName)
      throws NotFoundException;

  /**
   * Validate an application component with the application manager
   *
   * @param user          the user who performs the validation
   * @param appName       the name of the application
   * @param componentName the name of the application component
   * @throws NotFoundException when the application component cannot be found
   *                           in the application manager
   */
  void validateApplicationComponent(
      JaguarUser user, String appName, String componentName)
      throws NotFoundException;

  /**
   * Perform an action on the application
   *
   * @param user    the user who performs the action
   * @param context the execution context of the action
   * @param jsonDef the JSON definition of the action
   * @return action result
   */
  // TODO:
  // We should probably pass in an Action object instead of a context
  Properties performAction(
      JaguarUser user, Properties context, String jsonDef)
      throws ActionException;

  /**
   * Validate an action definition
   *
   * @param user    the user who performs the validation
   * @param app     application name
   * @param jsonDef action definition in json string
   */
  void validateAction(JaguarUser user, String app, String jsonDef)
      throws InvalidFormatException;

  Provider getProvider();
}
