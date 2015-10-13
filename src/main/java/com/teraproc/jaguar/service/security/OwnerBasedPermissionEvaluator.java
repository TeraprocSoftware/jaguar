package com.teraproc.jaguar.service.security;

import com.teraproc.jaguar.domain.Application;
import com.teraproc.jaguar.domain.JaguarUser;
import com.teraproc.jaguar.service.NotFoundException;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.io.Serializable;
import java.lang.reflect.Field;

@Component
public class OwnerBasedPermissionEvaluator implements PermissionEvaluator {

  private UserDetailsService userDetailsService;

  @Override
  public boolean hasPermission(
      Authentication authentication, final Object targetDomainObject,
      Object permission) {
    if (targetDomainObject == null) {
      throw new NotFoundException("Capacity not found.");
    }
    try {
      JaguarUser user = userDetailsService.getDetails(
          (String) authentication.getPrincipal(), UserFilterField.USERNAME);
      if (getUserId(targetDomainObject).equals(user.getId())) {
        return true;
      }
    } catch (IllegalAccessException e) {
      return false;
    }
    return false;
  }

  @Override
  public boolean hasPermission(
      Authentication authentication, Serializable targetId, String targetType,
      Object permission) {
    return false;
  }

  public void setUserDetailsService(UserDetailsService userDetailsService) {
    this.userDetailsService = userDetailsService;
  }

  private String getUserId(Object targetDomainObject)
      throws IllegalAccessException {
    Field clusterField =
        ReflectionUtils.findField(targetDomainObject.getClass(), "cluster");
    if (clusterField != null) {
      clusterField.setAccessible(true);
      Application application =
          (Application) clusterField.get(targetDomainObject);
      return getUserId(application);
    } else {
      Field userIdField =
          ReflectionUtils.findField(targetDomainObject.getClass(), "userId");
      if (userIdField != null) {
        userIdField.setAccessible(true);
        return (String) userIdField.get(targetDomainObject);
      }
      return getUserIdFromCluster(targetDomainObject);
    }
  }

  private String getUserIdFromCluster(Object targetDomainObject)
      throws IllegalAccessException {
    Field owner =
        ReflectionUtils.findField(targetDomainObject.getClass(), "user");
    owner.setAccessible(true);
    JaguarUser user = (JaguarUser) owner.get(targetDomainObject);
    return user.getId();
  }
}
