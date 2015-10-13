package com.teraproc.jaguar.provider.metrics;

import java.util.Collection;
import java.util.Map;

public class ElasticsearchMetricsRequest {
  private Collection<String> fields;
  private Query query;
  private Map<String, SortEntity> sort;

  public Collection<String> getFields() {
    return fields;
  }

  public void setFields(Collection<String> fields) {
    this.fields = fields;
  }

  public Query getQuery() {
    return query;
  }

  public void setQuery(Query query) {
    this.query = query;
  }

  public Map<String, SortEntity> getSort() {
    return sort;
  }

  public void setSort(
      Map<String, SortEntity> sort) {
    this.sort = sort;
  }

}
