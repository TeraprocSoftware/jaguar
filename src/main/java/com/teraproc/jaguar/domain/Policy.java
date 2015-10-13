package com.teraproc.jaguar.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;

@Entity
@NamedQueries({
    @NamedQuery(name = "Policy.findByApplication",
        query = "SELECT c FROM Policy c WHERE c.application.id= "
            + ":applicationId AND c.id= :policyId"),
    @NamedQuery(name = "Policy.findAllByApplication",
        query = "SELECT c FROM Policy c WHERE c.application.id= :id") })

public class Policy {
  private static final int DEFAULT_INTERVAL_IN_SECONDS = 30;

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE,
      generator = "policy_generator")
  @SequenceGenerator(name = "policy_generator", sequenceName = "sequence_table")
  private long id;

  @ManyToOne
  private Application application;

  @Column(name = "name")
  private String name;

  @Column(name = "description")
  private String description;

  @Column(name = "enabled")
  private boolean enabled;

  @Column(name = "interval")
  private int interval = DEFAULT_INTERVAL_IN_SECONDS;

  @Enumerated(EnumType.STRING)
  @Column(name = "scope")
  private Scope scope;

  @Column(name = "time_zone")
  private String timeZone;

  @Column(name = "cron")
  private String cron;

  @Column(name = "start_time")
  private String startTime;

  @Column(name = "duration")
  private String duration;

  @Column(name = "alert_definition", length = 4096)
  private String alertDefinition;

  @Column(name = "action_definition", length = 4096)
  private String actionDefinition;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Application getApplication() {
    return application;
  }

  public void setApplication(Application application) {
    this.application = application;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public int getInterval() {
    return interval;
  }

  public void setInterval(int interval) {
    this.interval = interval;
  }

  public Scope getScope() {
    return scope;
  }

  public void setScope(Scope policyScope) {
    this.scope = policyScope;
  }

  public String getTimeZone() {
    return timeZone;
  }

  public void setTimeZone(String timeZone) {
    this.timeZone = timeZone;
  }

  public String getCron() {
    return cron;
  }

  public void setCron(String cron) {
    this.cron = cron;
  }

  public String getStartTime() {
    return startTime;
  }

  public void setStartTime(String startTime) {
    this.startTime = startTime;
  }

  public String getDuration() {
    return duration;
  }

  public void setDuration(String duration) {
    this.duration = duration;
  }

  public String getAlertDefinition() {
    return alertDefinition;
  }

  public void setAlertDefinition(String alertDefinition) {
    this.alertDefinition = alertDefinition;
  }

  public String getActionDefinition() {
    return actionDefinition;
  }

  public void setActionDefinition(String actionDefinition) {
    this.actionDefinition = actionDefinition;
  }

}
