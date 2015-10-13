package com.teraproc.jaguar.domain;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MapKeyColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Entity
@NamedQueries({
    @NamedQuery(name = "History.findAllByApplication",
        query = "SELECT c FROM History c WHERE c.applicationId= :id"),
    @NamedQuery(name = "History.findByApplication",
        query = "SELECT c FROM History c WHERE c.applicationId= "
            + ":applicationId AND c.id= :historyId") })

public class History {
  public static final String ALERT_DEFINITION = "alertDefinition";
  public static final String PERIOD = "period";
  public static final String START_TIME = "startTime";
  public static final String DURATION = "duration";
  public static final String CRON = "cron";
  public static final String TIME_ZONE = "timeZone";
  public static final String ACTION_DEFINITION = "actionDefinition";

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE,
      generator = "history_generator")
  @SequenceGenerator(name = "history_generator",
      sequenceName = "sequence_table")
  private long id;

  @Column(name = "user_id")
  private String userId;

  @Column(name = "application_id")
  private long applicationId;

  @Column(name = "policy_id")
  private long policyId;

  @Column(name = "application")
  private String application;

  @Column(name = "policy")
  private String policy;

  @Enumerated(EnumType.STRING)
  private Scope scope;

  private long timestamp;

  @ElementCollection(fetch = FetchType.EAGER)
  @MapKeyColumn(name = "key")
  @Column(name = "value", columnDefinition = "TEXT", length = 100000)
  private Map<String, String> properties = new HashMap<>();


  public History() {
    this.timestamp = System.currentTimeMillis();
  }

  public History withPolicy(Policy policy) {
    this.policyId = policy.getId();
    this.applicationId = policy.getApplication().getId();
    this.userId = policy.getApplication().getUser().getId();
    this.application = policy.getApplication().getName();
    this.policy = policy.getName();
    this.scope = policy.getScope();
    this.properties.put(ALERT_DEFINITION, policy.getAlertDefinition());
    this.properties.put(PERIOD, String.valueOf(policy.getInterval()));
    this.properties.put(START_TIME, policy.getStartTime());
    this.properties.put(DURATION, String.valueOf(policy.getDuration()));
    this.properties.put(CRON, String.valueOf(policy.getCron()));
    this.properties.put(TIME_ZONE, String.valueOf(policy.getTimeZone()));
    return this;
  }

  public History withAction(String def) {
    this.properties.put(ACTION_DEFINITION, def);
    return this;
  }

  public History withActionResult(Properties props) {
    Enumeration e = props.propertyNames();
    while (e.hasMoreElements()) {
      String key = (String) e.nextElement();
      this.properties.put(key, props.getProperty(key));
    }
    return this;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public long getApplicationId() {
    return applicationId;
  }

  public void setApplicationId(long applicationId) {
    this.applicationId = applicationId;
  }

  public long getPolicyId() {
    return policyId;
  }

  public void setPolicyId(long policyId) {
    this.policyId = policyId;
  }

  public String getApplication() {
    return application;
  }

  public void setApplication(String application) {
    this.application = application;
  }

  public String getPolicy() {
    return policy;
  }

  public void setPolicy(String policy) {
    this.policy = policy;
  }

  public Scope getScope() {
    return scope;
  }

  public void setScope(Scope scope) {
    this.scope = scope;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }
}
