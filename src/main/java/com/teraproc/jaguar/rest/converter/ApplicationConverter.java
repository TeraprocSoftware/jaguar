package com.teraproc.jaguar.rest.converter;

import com.teraproc.jaguar.domain.Application;
import com.teraproc.jaguar.rest.json.ApplicationJson;
import org.springframework.stereotype.Component;

@Component
public class ApplicationConverter
    extends AbstractConverter<ApplicationJson, Application> {

  @Override
  public Application convert(ApplicationJson source) {
    Application application = new Application();
    application.setName(source.getName());
    application.setProvider(source.getProvider());
    application.setEnabled(Boolean.valueOf(source.getEnabled()));
    return application;
  }

  public Application update(Application application, ApplicationJson json) {
    application.setProvider(json.getProvider());
    application.setName(json.getName());
    application.setEnabled(Boolean.valueOf(json.getEnabled()));
    return application;
  }

  @Override
  public ApplicationJson convert(Application application) {
    ApplicationJson json = new ApplicationJson();
    json.setId(application.getId());
    json.setName(application.getName());
    json.setProvider(application.getProvider());
    json.setEnabled(application.getEnabled());
    return json;
  }
}
