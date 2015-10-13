package com.teraproc.jaguar.provider.metrics;

public class Query {
  private Filtered filtered;

  public Query(Filtered filtered) {
    this.filtered = filtered;
  }

  public Filtered getFiltered() {
    return filtered;
  }

  public void setFiltered(
      Filtered filtered) {
    this.filtered = filtered;
  }

}
