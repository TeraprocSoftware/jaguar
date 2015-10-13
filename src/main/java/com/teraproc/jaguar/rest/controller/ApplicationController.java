package com.teraproc.jaguar.rest.controller;

import com.teraproc.jaguar.domain.Application;
import com.teraproc.jaguar.domain.JaguarUser;
import com.teraproc.jaguar.domain.Provider;
import com.teraproc.jaguar.log.JaguarLoggerFactory;
import com.teraproc.jaguar.log.Logger;
import com.teraproc.jaguar.rest.converter.ApplicationConverter;
import com.teraproc.jaguar.rest.json.ApplicationJson;
import com.teraproc.jaguar.service.ApplicationService;
import com.teraproc.jaguar.service.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/applications")
public class ApplicationController {

  private static final Logger LOGGER =
      JaguarLoggerFactory.getLogger(ApplicationController.class);

  @Autowired
  private ApplicationService applicationService;
  @Autowired
  private ApplicationConverter applicationConverter;

  @RequestMapping(method = RequestMethod.POST)
  public ResponseEntity<ApplicationJson> createApplication(
      @ModelAttribute("user") JaguarUser user,
      @RequestBody ApplicationJson json) {
    return registerApplication(user, json, null);
  }

  @RequestMapping(value = "/{applicationId}", method = RequestMethod.PUT)
  public ResponseEntity<ApplicationJson> modifyApplication(
      @ModelAttribute("user") JaguarUser user
      , @RequestBody ApplicationJson json, @PathVariable long applicationId) {
    return registerApplication(user, json, applicationId);
  }

  @RequestMapping(method = RequestMethod.GET)
  public ResponseEntity<List<ApplicationJson>> getApplications(
      @ModelAttribute("user") JaguarUser user) {
    List<Application> applications = applicationService.findAllByUser(user);
    return new ResponseEntity<>(
        applicationConverter.convertAllToJson(applications), HttpStatus.OK);
  }

  @RequestMapping(value = "/{applicationId}", method = RequestMethod.GET)
  public ResponseEntity<ApplicationJson> getApplication(
      @ModelAttribute("user") JaguarUser user,
      @PathVariable long applicationId) {
    Application application =
        applicationService.findOneByUser(user, applicationId);
    return createApplicationJsonResponse(application);
  }

  @RequestMapping(value = "/{applicationId}", method = RequestMethod.DELETE)
  public ResponseEntity<ApplicationJson> deleteApplication(
      @ModelAttribute("user") JaguarUser user,
      @PathVariable long applicationId) {
    applicationService.remove(user, applicationId);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  private ResponseEntity<ApplicationJson> createApplicationJsonResponse(
      Application application) {
    return createApplicationJsonResponse(application, HttpStatus.OK);
  }

  private ResponseEntity<ApplicationJson> createApplicationJsonResponse(
      Application application, HttpStatus status) {
    return new ResponseEntity<>(
        applicationConverter.convert(application), status);
  }

  private ResponseEntity<ApplicationJson> registerApplication(
      JaguarUser user, ApplicationJson json, Long applicationId) {
    // Check application provider. Now only support SLIDER.
    if (json.getProvider() == null) {
      json.setProvider(Provider.SLIDER);
    } else if (!Provider.SLIDER.equals(json.getProvider())) {
      throw new NotFoundException("Unsupported application provider "
          + json.getProvider());
    }
    // Register a new application
    if (applicationId == null) {
      return createApplicationJsonResponse(
          applicationService.create(user, json), HttpStatus.CREATED);
    }
    // Update an existing application
    return createApplicationJsonResponse(
        applicationService.update(user, applicationId, json), HttpStatus.OK);
  }
}

