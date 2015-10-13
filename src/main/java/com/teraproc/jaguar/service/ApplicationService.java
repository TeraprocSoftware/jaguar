package com.teraproc.jaguar.service;

import com.teraproc.jaguar.domain.Application;
import com.teraproc.jaguar.domain.JaguarUser;
import com.teraproc.jaguar.domain.Provider;
import com.teraproc.jaguar.provider.manager.ApplicationManager;
import com.teraproc.jaguar.repository.ApplicationRepository;
import com.teraproc.jaguar.repository.UserRepository;
import com.teraproc.jaguar.rest.converter.ApplicationConverter;
import com.teraproc.jaguar.rest.json.ApplicationJson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class ApplicationService {
  @Resource
  private Map<Provider, ApplicationManager> applicationManagers;

  @Autowired
  private ApplicationRepository applicationRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ApplicationConverter applicationConverter;

  public Application create(JaguarUser user, ApplicationJson json) {
    try {
      applicationManagers.get(json.getProvider())
          .validateApplication(user, json.getName());
    } catch (NotFoundException e) {
      throw e;
    } catch (Exception e) {
      // Catch exception thrown from user provided implementation
      throw new RuntimeException(e);
    }
    JaguarUser jaguarUser = createUserIfAbsent(user);
    Application application = applicationConverter.convert(json);
    application.setUser(jaguarUser);
    application = save(application);
    return application;
  }

  public Application update(
      JaguarUser user, long applicationId, ApplicationJson json) {
    Application application = findOneByUser(user, applicationId);
    if (application == null || !application.getUser().getId()
        .equals(user.getId())) {
      throw new NotFoundException(
          "Application " + applicationId + " not found");
    }
    application = applicationConverter.update(application, json);
    return save(application);
  }

  public List<Application> findAllByUser(JaguarUser user) {
    return applicationRepository.findAllByUser(user.getId());
  }

  public List<Application> findAll() {
    return applicationRepository.findAll();
  }

  public Application findOneByUser(JaguarUser user, long applicationId) {
    Application application = applicationRepository.findOne(applicationId);
    if (application == null || !application.getUser().getId()
        .equals(user.getId())) {
      throw new NotFoundException(
          "Application " + applicationId + " not found");
    }
    return application;
  }

  public Application save(Application application) {
    return applicationRepository.save(application);
  }

  public Application find(long applicationId) {
    return applicationRepository.find(applicationId);
  }

  public List<Application> findAllEnabled() {
    return applicationRepository.findAllByState(true);
  }

  public void remove(JaguarUser user, long applicationId) {
    Application application = findOneByUser(user, applicationId);
    applicationRepository.delete(application);
  }

  private JaguarUser createUserIfAbsent(JaguarUser user) {
    JaguarUser jaguarUser = userRepository.findOne(user.getId());
    if (jaguarUser == null) {
      jaguarUser = userRepository.save(user);
    }
    return jaguarUser;
  }
}
