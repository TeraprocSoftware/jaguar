package com.teraproc.jaguar.rest.json;

import com.teraproc.jaguar.domain.Provider;

public class ApplicationJson implements Json {
  private long id;
  private String name;
  private Provider provider;
  private boolean enabled;

  public ApplicationJson() {
  }

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

  public Provider getProvider() {
    return provider;
  }

  public void setProvider(Provider provider) {
    this.provider = provider;
  }

  public boolean getEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }
}
