package com.teraproc.jaguar.service;

import com.teraproc.jaguar.domain.Action;
import com.teraproc.jaguar.domain.Application;
import com.teraproc.jaguar.domain.Provider;
import com.teraproc.jaguar.domain.BaseAlert;
import com.teraproc.jaguar.domain.Condition;
import com.teraproc.jaguar.domain.GroupAlert;
import com.teraproc.jaguar.domain.InstanceAlert;
import com.teraproc.jaguar.domain.InternalPolicy;
import com.teraproc.jaguar.domain.JaguarUser;
import com.teraproc.jaguar.domain.Policy;
import com.teraproc.jaguar.domain.Scope;
import com.teraproc.jaguar.provider.manager.ApplicationManager;
import com.teraproc.jaguar.repository.PolicyRepository;
import com.teraproc.jaguar.rest.converter.PolicyConverter;
import com.teraproc.jaguar.rest.json.PolicyJson;
import com.teraproc.jaguar.utils.NumberUtils;
import com.teraproc.jaguar.utils.TreeNode;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

@Service
public class PolicyService {

  private static final String delims = "+,-,*,/,>,<,>=,<=,==,!=,(,), ";
  private static ObjectMapper mapper = new ObjectMapper();
  private static JsonFactory factory = mapper.getJsonFactory();

  @Resource
  private Map<Provider, ApplicationManager> applicationManagers;

  @Autowired
  private ApplicationService applicationService;

  @Autowired
  private PolicyRepository policyRepository;

  @Autowired
  private PolicyConverter policyConverter;

  /* <appID, <policyID, Policy>> */
  private Map<Long, Map<Long, InternalPolicy>> policies = new HashMap<>();

  @PostConstruct
  public void init() {
    List<Application> apps = applicationService.findAll();
    for (Application app : apps) {
      this.policies.put(app.getId(), new HashMap<Long, InternalPolicy>());
      List<Policy> policies =
          policyRepository.findAllByApplication(app.getId());
      for (Policy policy : policies) {
        this.policies.get(app.getId()).put(policy.getId(), parsePolicy(policy));
      }
    }
  }

  public Policy createPolicy(
      JaguarUser user, long applicationId, Policy policy, Scope scope) {
    Application application =
        applicationService.findOneByUser(user, applicationId);
    policy.setApplication(application);
    policy.setScope(scope);
    InternalPolicy internalPolicy = parsePolicy(policy);

    // save policy first, policyRepository assigns policy ID
    policy = policyRepository.save(policy);

    // cache internal policy
    if (!policies.containsKey(applicationId)) {
      policies.put(applicationId, new HashMap<Long, InternalPolicy>());
    }
    policies.get(applicationId).put(policy.getId(), internalPolicy);
    return policy;
  }

  public Policy updatePolicy(
      JaguarUser user, long applicationId, long policyId, PolicyJson json) {
    applicationService.findOneByUser(user, applicationId);
    Policy policy = getExistingPolicy(applicationId, policyId);
    policy = policyConverter.update(policy, json);
    InternalPolicy internalPolicy = parsePolicy(policy);
    policy = policyRepository.save(policy);
    // cache internal policy
    if (!policies.containsKey(applicationId)) {
      policies.put(applicationId, new HashMap<Long, InternalPolicy>());
    }
    policies.get(applicationId).put(policy.getId(), internalPolicy);
    return policy;
  }

  public void deletePolicy(JaguarUser user, long applicationId, long policyId) {
    applicationService.findOneByUser(user, applicationId);
    Policy policy = getExistingPolicy(applicationId, policyId);
    if (policies.containsKey(applicationId)) {
      policies.get(applicationId).remove(policyId);
    }
    policyRepository.delete(policy);
  }

  public List<Policy> getPolicies(
      JaguarUser user, long applicationId, Scope scope) {
    applicationService.findOneByUser(user, applicationId);
    List<Policy> policies =
        policyRepository.findAllByApplication(applicationId);
    for (Policy policy : policies) {
      if (!policy.getScope().equals(scope)) {
        policies.remove(policy);
      }
    }
    return policies;
  }

  public Policy getPolicy(JaguarUser user, long applicationId, long policyId) {
    applicationService.findOneByUser(user, applicationId);
    return policyRepository.findByApplication(applicationId, policyId);
  }

  private Policy getExistingPolicy(long applicationId, long policyId) {
    Policy policy = policyRepository.findByApplication(applicationId, policyId);
    if (policy == null) {
      throw new NotFoundException("Policy " + policyId + " not found");
    }
    return policy;
  }

  public InternalPolicy getInternalPolicy(long applicationId, long policyId) {
    return
        policies.get(applicationId) == null ? null : policies.get(applicationId)
            .get(policyId);
  }


  public List<InternalPolicy> getInternalPolicies(long applicationId) {
    return policies.get(applicationId) == null ? new ArrayList<InternalPolicy>()
        : new ArrayList<>(policies.get(applicationId).values());
  }

  public InternalPolicy parsePolicy(Policy policy) {
    InternalPolicy internalPolicy = new InternalPolicy(policy);
    BaseAlert alert = parseAlert(policy);
    List<Action> actions = getActions(policy);
    long duration = convertDuration(policy.getDuration());
    // no parsing error, update internalPolicy now
    internalPolicy.setAlert(alert);
    internalPolicy.setActions(actions);
    internalPolicy.setDurationInSeconds(duration);
    return internalPolicy;
  }

  private BaseAlert parseAlert(Policy policy) {
    return policy.getScope().equals(Scope.INSTANCE) ? parseInstanceAlert(
        policy) : parseApplicationAlert(policy);
  }

  private InstanceAlert parseInstanceAlert(Policy policy) {
    try {
      InstanceAlert alert =
          mapper.readValue(policy.getAlertDefinition(), InstanceAlert.class);
      if (alert.getCondition() == null
          || alert.getCondition().getExpression() == null
          || alert.getCondition().getComponentName() == null) {
        throw new Exception();
      }
      // temp fix
      if (alert.getCondition().getAggregates() == null) {
        alert.getCondition().setAggregates();
      }
      Application application = policy.getApplication();
      applicationManagers.get(application.getProvider())
          .validateApplicationComponent(
              application.getUser(), application.getName(),
              alert.getCondition().getComponentName());
      alert.setCondition(parseConditionExpr(alert.getCondition()));
      return alert;
    } catch (Exception e) {
      throw new InvalidFormatException(
          "Unrecognized alert format due to: " + e.getMessage(), e.getCause());
    }
  }

  private GroupAlert parseApplicationAlert(Policy policy) {
    try {
      GroupAlert alert = new GroupAlert();
      JsonParser jp = factory.createJsonParser(policy.getAlertDefinition());
      JsonNode node = mapper.readTree(jp);
      if (node.get("successiveIntervals") == null) {
        throw new InvalidFormatException(
            "Unrecognized alert format due to: 'successiveIntervals' is "
                + "required");
      }
      alert.setSuccessiveIntervals(
          node.get("successiveIntervals").getIntValue());

      alert.setRoot(getConditionNode(node, policy));
      return alert;
    } catch (InvalidFormatException fe) {
      throw fe;
    } catch (Exception e) {
      throw new InvalidFormatException(
          "Unrecognized alert format due to: " + e.getMessage(), e.getCause());
    }
  }

  private TreeNode<Condition> getConditionNode(
      JsonNode rootNode, Policy policy) {
    // leaf node
    // "condition": {...}
    if (rootNode.get("condition") != null) {
      try {
        Condition condition =
            mapper.readValue(rootNode.get("condition"), Condition.class);
        Application application = policy.getApplication();
        applicationManagers.get(application.getProvider())
            .validateApplicationComponent(
                application.getUser(), application.getName(),
                condition.getComponentName());
        condition = parseConditionExpr(condition);
        return new TreeNode<>(condition);
      } catch (Exception e) {
        throw new InvalidFormatException(
            "Unrecognized alert condition format due to: " + e.getMessage(),
            e.getCause());
      }
    } else if (rootNode.get("and") != null || rootNode.get("or") != null) {
      // internal node
      // "and": ["condition":{...}, "or": ["condition":"...",
      // "condition":"..."]]
      List<TreeNode<Condition>> childList = new ArrayList<>();
      String operator = rootNode.get("and") != null ? "and" : "or";
      rootNode = rootNode.get(operator);

      for (final JsonNode jsonNode : rootNode) {
        childList.add(getConditionNode(jsonNode, policy));
      }
      TreeNode<Condition> parent = new TreeNode<>(new Condition(operator));
      for (TreeNode<Condition> child : childList) {
        parent.addChildNode(child);
      }
      return parent;
    } else {
      throw new InvalidFormatException(
          "Unrecognized alert format: only allow 'condition', 'and', 'or'");
    }
  }

  private List<Action> getActions(Policy policy) {
    try {
      List<Action> actions = new ArrayList<>();
      JsonParser jp = factory.createJsonParser(policy.getActionDefinition());
      JsonNode arrayNodes = mapper.readTree(jp);
      for (final JsonNode jsonNode : arrayNodes) {
        String actionDef = mapper.writeValueAsString(jsonNode);
        Application application = policy.getApplication();
        ApplicationManager appManager =
            applicationManagers.get(application.getProvider());
        appManager.validateAction(
            application.getUser(), policy.getApplication().getName(),
            actionDef);
        Action action = new Action(actionDef);
        action.setApplicationManager(appManager);
        if (jsonNode.has("cooldown")) {
          action.setCooldown(jsonNode.get("cooldown").getIntValue());
        }
        actions.add(action);
      }
      return actions;
    } catch (InvalidFormatException fe) {
      throw fe;
    } catch (Exception e) {
      throw new InvalidFormatException(
          "Unrecognized actions format due to: " + e.getMessage(),
          e.getCause());
    }
  }

  private long convertDuration(String duration) {
    // duration format is [n]H[n]M[n]S
    long hour = Long.valueOf(duration.substring(0, duration.indexOf('H')));
    long minute = Long.valueOf(
        duration.substring(duration.indexOf('H') + 1, duration.indexOf('M')));
    long second = Long.valueOf(
        duration.substring(duration.indexOf('M') + 1, duration.indexOf('S')));
    return hour * 3600 + minute * 60 + second;
  }

  private Condition parseConditionExpr(Condition condition) {
    if (condition.getExpression() == null) {
      throw new InvalidFormatException(
          "Unrecognized alert condition format: 'expression' is required");
    }
        /* Metric name must not include character defined in delima */
    StringTokenizer st =
        new StringTokenizer(condition.getExpression(), delims);
    while (st.hasMoreElements()) {
      String element = (String) st.nextElement();
      if (!NumberUtils.isNumeric(element)) {
        if (condition.getAggregates().containsKey(element)) {
          String nextElement = (String) st.nextElement();
                    /* TODO: Better to verify the nextElement */
          condition.getAggregates().get(element).put(nextElement, 0);
          if (!condition.getMetrics().contains(nextElement)) {
            condition.getMetrics().add(nextElement);
          }
        } else if (!condition.getMetrics().contains(element)) {
          condition.getMetrics().add(element);
        }
      }
    }
    return condition;
  }
}
