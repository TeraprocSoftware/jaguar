package com.teraproc.jaguar.rest.controller;

import com.teraproc.jaguar.domain.JaguarUser;
import com.teraproc.jaguar.domain.Policy;
import com.teraproc.jaguar.domain.Scope;
import com.teraproc.jaguar.rest.converter.PolicyConverter;
import com.teraproc.jaguar.rest.json.PolicyJson;
import com.teraproc.jaguar.service.InvalidFormatException;
import com.teraproc.jaguar.service.PolicyService;
import com.teraproc.jaguar.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.text.ParseException;
import java.util.List;

@RestController
@RequestMapping("/v1/applications/{applicationId}/policies")
public class PolicyController {

  @Autowired
  private PolicyService policyService;
  @Autowired
  private PolicyConverter policyConverter;

  @RequestMapping(value = "/instance", method = RequestMethod.POST)
  public ResponseEntity<PolicyJson> createInstancePolicy(
      @ModelAttribute("user") JaguarUser user
      , @PathVariable long applicationId, @RequestBody @Valid PolicyJson json) {
    validatePolicyJson(json);
    Policy policy = policyConverter.convert(json);
    return createPolicyResponse(
        policyService.createPolicy(user, applicationId, policy, Scope.INSTANCE),
        HttpStatus.CREATED);
  }

  @RequestMapping(value = "/instance", method = RequestMethod.GET)
  public ResponseEntity<List<PolicyJson>> getInstancePolicies(
      @ModelAttribute("user") JaguarUser user,
      @PathVariable long applicationId) {
    return createPoliciesResponse(
        policyService.getPolicies(user, applicationId, Scope.INSTANCE));
  }

  @RequestMapping(value = "/instance/{policyId}", method = RequestMethod.GET)
  public ResponseEntity<PolicyJson> getInstancePolicy(
      @ModelAttribute("user") JaguarUser user, @PathVariable long applicationId,
      @PathVariable long policyId) {
    return createPolicyResponse(
        policyService.getPolicyByScope(
            user, applicationId, policyId, Scope.INSTANCE), HttpStatus.OK);
  }

  @RequestMapping(value = "/instance/{policyId}", method = RequestMethod.PUT)
  public ResponseEntity<PolicyJson> updateInstancePolicy(
      @ModelAttribute("user") JaguarUser user
      , @PathVariable long applicationId, @PathVariable long policyId,
      @RequestBody @Valid PolicyJson json) {
    validatePolicyJson(json);
    return createPolicyResponse(
        policyService.updatePolicy(user, applicationId, policyId, json),
        HttpStatus.OK);
  }

  @RequestMapping(value = "/instance/{policyId}", method = RequestMethod.DELETE)
  public ResponseEntity<PolicyJson> deleteInstancePolicy(
      @ModelAttribute("user") JaguarUser user
      , @PathVariable long applicationId, @PathVariable long policyId) {
    policyService.deletePolicy(user, applicationId, policyId);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @RequestMapping(value = "/group", method = RequestMethod.POST)
  public ResponseEntity<PolicyJson> createGroupPolicy(
      @ModelAttribute("user") JaguarUser user
      , @PathVariable long applicationId, @RequestBody @Valid PolicyJson json) {
    validatePolicyJson(json);
    Policy policy = policyConverter.convert(json);
    return createPolicyResponse(
        policyService.createPolicy(user, applicationId, policy, Scope.GROUP),
        HttpStatus.CREATED);
  }

  @RequestMapping(value = "/group", method = RequestMethod.GET)
  public ResponseEntity<List<PolicyJson>> getGroupPolicies(
      @ModelAttribute("user") JaguarUser user,
      @PathVariable long applicationId) {
    return createPoliciesResponse(
        policyService.getPolicies(user, applicationId, Scope.GROUP));
  }

  @RequestMapping(value = "/group/{policyId}", method = RequestMethod.GET)
  public ResponseEntity<PolicyJson> getGroupPolicy(
      @ModelAttribute("user") JaguarUser user, @PathVariable long applicationId,
      @PathVariable long policyId) {
    return createPolicyResponse(
        policyService
            .getPolicyByScope(user, applicationId, policyId, Scope.GROUP),
        HttpStatus.OK);
  }

  @RequestMapping(value = "/group/{policyId}", method = RequestMethod.PUT)
  public ResponseEntity<PolicyJson> updateGroupPolicy(
      @ModelAttribute("user") JaguarUser user
      , @PathVariable long applicationId, @PathVariable long policyId,
      @RequestBody @Valid PolicyJson json) {
    validatePolicyJson(json);
    return createPolicyResponse(
        policyService.updatePolicy(user, applicationId, policyId, json),
        HttpStatus.OK);
  }

  @RequestMapping(value = "/group/{policyId}", method = RequestMethod.DELETE)
  public ResponseEntity<PolicyJson> deleteGroupPolicy(
      @ModelAttribute("user") JaguarUser user, @PathVariable long applicationId,
      @PathVariable long policyId) {
    policyService.deletePolicy(user, applicationId, policyId);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @RequestMapping(method = RequestMethod.GET)
  public ResponseEntity<List<PolicyJson>> getPolicies(
      @ModelAttribute("user") JaguarUser user,
      @PathVariable long applicationId) {
    return createPoliciesResponse(
        policyService.getPolicies(user, applicationId, null));
  }

  @RequestMapping(value = "/{policyId}", method = RequestMethod.GET)
  public ResponseEntity<PolicyJson> getPolicy(
      @ModelAttribute("user") JaguarUser user, @PathVariable long applicationId,
      @PathVariable long policyId) {
    return createPolicyResponse(
        policyService.getPolicy(
            user, applicationId, policyId), HttpStatus.OK);
  }

  private void validatePolicyJson(PolicyJson json) {
    try {
      DateUtils.getCronExpression("* * * " + json.getCron());
      DateUtils.getTimeZone(json.getTimezone());
    } catch (ParseException e) {
      throw new InvalidFormatException(e.getMessage(), e.getCause());
    }
  }

  private ResponseEntity<PolicyJson> createPolicyResponse(
      Policy policy, HttpStatus status) {
    PolicyJson policyResponse = policyConverter.convert(policy);
    return new ResponseEntity<>(policyResponse, status);
  }

  private ResponseEntity<List<PolicyJson>> createPoliciesResponse(
      List<Policy> policies) {
    List<PolicyJson> policiesJson = policyConverter.convertAllToJson(policies);
    return new ResponseEntity<>(policiesJson, HttpStatus.OK);
  }

}
