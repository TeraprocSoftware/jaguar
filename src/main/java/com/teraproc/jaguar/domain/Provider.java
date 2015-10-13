package com.teraproc.jaguar.domain;

public enum Provider {
  SLIDER("SLIDER");

  private final String provider;

  Provider(String provider) {
    this.provider = provider;
  }

  public String getProvider() {
    return this.provider;
  }
}
