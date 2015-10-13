package com.teraproc.jaguar.provider.metrics;

import java.util.Collection;

public class Filter {
  private Collection<ExistsEntity> or;

  public Collection<ExistsEntity> getOr() {
    return or;
  }

  public void setOr(
      Collection<ExistsEntity> or) {
    this.or = or;
  }
}
