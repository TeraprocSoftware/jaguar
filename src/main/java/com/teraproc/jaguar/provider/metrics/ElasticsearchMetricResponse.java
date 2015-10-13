package com.teraproc.jaguar.provider.metrics;

import java.util.Map;

public class ElasticsearchMetricResponse {
  private int took;
  private boolean timed_out;
  private Map<String, Integer> _shards;
  private ElasticsearchHits hits;

  public int getTook() {
    return took;
  }

  public void setTook(int took) {
    this.took = took;
  }

  public boolean isTimed_out() {
    return timed_out;
  }

  public void setTimed_out(boolean timed_out) {
    this.timed_out = timed_out;
  }

  public Map<String, Integer> get_shards() {
    return _shards;
  }

  public void set_shards(Map<String, Integer> _shards) {
    this._shards = _shards;
  }

  public ElasticsearchHits getHits() {
    return hits;
  }

  public void setHits(ElasticsearchHits hits) {
    this.hits = hits;
  }
}
