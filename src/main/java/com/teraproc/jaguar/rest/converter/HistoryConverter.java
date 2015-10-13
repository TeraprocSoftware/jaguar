package com.teraproc.jaguar.rest.converter;

import com.teraproc.jaguar.domain.History;
import com.teraproc.jaguar.rest.json.HistoryJson;
import org.springframework.stereotype.Component;

@Component
public class HistoryConverter extends AbstractConverter<HistoryJson, History> {

  @Override
  public HistoryJson convert(History source) {
    HistoryJson json = new HistoryJson();
    json.setId(source.getId());
    json.setUser(source.getUserId());
    json.setApplicationId(source.getApplicationId());
    json.setPolicyId(source.getPolicyId());
    json.setApplication(source.getApplication());
    json.setPolicy(source.getPolicy());
    json.setScope(source.getScope());
    json.setTimestamp(source.getTimestamp());
    json.setProperties(source.getProperties());
    return json;
  }
}
