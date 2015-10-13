package com.teraproc.jaguar.rest.json;

import javax.validation.constraints.Pattern;

public class PolicyJson implements Json {
  private Long id;
  @Pattern(regexp = "([a-zA-Z][-a-zA-Z0-9]*)",
      message = "The name can only contain alphanumeric characters and "
          + "hyphens and has start with an alphanumeric character")
  private String name;
  private String description;
  private boolean enabled;
  private int interval;
  private String timezone;
  private String corn;
  @Pattern(regexp = "(([01]?[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9])",
      message = "The start time format must be hh:mm:ss")
  private String startTime;
  @Pattern(regexp = "(([1-9]+[0-9]|0)*H[0-5]?[0-9]M[0-5]?[0-9]S)",
      message = "The duration format must be [n]H[n]M[n]S, where [n] is "
          + "replaced by integer")
  private String duration;
  private Object alert;
  private Object actions;
  private long applicationId;

  public PolicyJson() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
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

  public String getTimezone() {
    return timezone;
  }

  public void setTimezone(String timezone) {
    this.timezone = timezone;
  }

  public String getCorn() {
    return corn;
  }

  public void setCorn(String corn) {
    this.corn = corn;
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

  public Object getAlert() {
    return alert;
  }

  public void setAlert(Object alert) {
    this.alert = alert;
  }

  public Object getActions() {
    return actions;
  }

  public void setActions(Object actions) {
    this.actions = actions;
  }

  public long getApplicationId() {
    return applicationId;
  }

  public void setApplicationId(long applicationId) {
    this.applicationId = applicationId;
  }
}
