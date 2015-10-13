package com.teraproc.jaguar.provider.metrics;

public class Filtered {
  private Filter filter;
  private Range query;

  public Filtered(Filter filter, Range query) {
    this.filter = filter;
    this.query = query;
  }

  public Filter getFilter() {
    return filter;
  }

  public void setFilter(Filter filter) {
    this.filter = filter;
  }

  public Range getQuery() {
    return query;
  }

  public void setQuery(Range query) {
    this.query = query;
  }
}
