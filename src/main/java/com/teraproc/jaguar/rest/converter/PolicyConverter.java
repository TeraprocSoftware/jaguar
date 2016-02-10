package com.teraproc.jaguar.rest.converter;

import com.teraproc.jaguar.domain.Policy;
import com.teraproc.jaguar.rest.json.PolicyJson;
import com.teraproc.jaguar.service.InvalidFormatException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class PolicyConverter extends AbstractConverter<PolicyJson, Policy> {
  private static ObjectMapper mapper = new ObjectMapper();

  @Override
  public Policy convert(PolicyJson source) {
    Policy policy = new Policy();
    policy.setName(source.getName());
    policy.setDescription(source.getDescription());
    policy.setEnabled(source.isEnabled());
    policy.setInterval(source.getInterval());
    policy.setTimeZone(source.getTimezone());
    policy.setCron(source.getCron());
    policy.setStartTime(source.getStartTime());
    policy.setDuration(source.getDuration());
    try {
      policy.setAlertDefinition(mapper.writeValueAsString(source.getAlert()));
    } catch (Exception e) {
      throw new InvalidFormatException("Unrecognized alert format");
    }
    try {
      policy.setActionDefinition(
          mapper.writeValueAsString(source.getActions()));
    } catch (Exception e) {
      throw new InvalidFormatException("Unrecognized actions format");
    }
    return policy;
  }

  public Policy update(Policy policy, PolicyJson source) {
    policy.setName(source.getName());
    policy.setDescription(source.getDescription());
    policy.setEnabled(source.isEnabled());
    policy.setInterval(source.getInterval());
    policy.setTimeZone(source.getTimezone());
    policy.setCron(source.getCron());
    policy.setStartTime(source.getStartTime());
    policy.setDuration(source.getDuration());
    try {
      policy.setAlertDefinition(mapper.writeValueAsString(source.getAlert()));
    } catch (Exception e) {
      throw new InvalidFormatException("Unrecognized alert format");
    }
    try {
      policy
          .setActionDefinition(mapper.writeValueAsString(source.getActions()));
    } catch (Exception e) {
      throw new InvalidFormatException("Unrecognized actions format");
    }
    return policy;
  }

  @Override
  public PolicyJson convert(Policy source) {
    PolicyJson json = new PolicyJson();
    json.setId(source.getId());
    json.setName(source.getName());
    json.setDescription(source.getDescription());
    json.setEnabled(source.isEnabled());
    json.setInterval(source.getInterval());
    json.setTimezone(source.getTimeZone());
    json.setCron(source.getCron());
    json.setStartTime(source.getStartTime());
    json.setDuration(source.getDuration());
    try {
      json.setAlert(
          mapper.readValue(source.getAlertDefinition(), Object.class));
    } catch (Exception e) {
      throw new InvalidFormatException("Unrecognized alert format");
    }
    try {
      json.setActions(
          mapper.readValue(source.getActionDefinition(), Object.class));
    } catch (Exception e) {
      throw new InvalidFormatException("Unrecognized actions format");
    }
    json.setApplicationId(source.getApplication().getId());
    return json;
  }
}
