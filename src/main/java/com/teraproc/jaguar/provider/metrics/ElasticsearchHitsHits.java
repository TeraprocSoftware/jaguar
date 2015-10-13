package com.teraproc.jaguar.provider.metrics;

import java.util.Collection;
import java.util.Map;

public class ElasticsearchHitsHits {
  private String _index;
  private String _type;
  private String _id;
  private String _score;
  private Map<String, Collection<String>> fields;
  private Collection<Number> sort;

  public String get_index() {
    return _index;
  }

  public void set_index(String _index) {
    this._index = _index;
  }

  public String get_type() {
    return _type;
  }

  public void set_type(String _type) {
    this._type = _type;
  }

  public String get_id() {
    return _id;
  }

  public void set_id(String _id) {
    this._id = _id;
  }

  public String get_score() {
    return _score;
  }

  public void set_score(String _score) {
    this._score = _score;
  }

  public Map<String, Collection<String>> getFields() {
    return fields;
  }

  public void setFields(Map<String, Collection<String>> fields) {
    this.fields = fields;
  }

  public Collection<Number> getSort() {
    return sort;
  }

  public void setSort(Collection<Number> sort) {
    this.sort = sort;
  }
}
