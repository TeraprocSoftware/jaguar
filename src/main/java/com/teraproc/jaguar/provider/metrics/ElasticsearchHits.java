package com.teraproc.jaguar.provider.metrics;

import java.util.Collection;

public class ElasticsearchHits {
  private int total;
  private String max_score;
  private Collection<ElasticsearchHitsHits> hits;

  public int getTotal() {
    return total;
  }

  public void setTotal(int total) {
    this.total = total;
  }

  public String getMax_score() {
    return max_score;
  }

  public void setMax_score(String max_score) {
    this.max_score = max_score;
  }

  public Collection<ElasticsearchHitsHits> getHits() {
    return hits;
  }

  public void setHits(Collection<ElasticsearchHitsHits> hits) {
    this.hits = hits;
  }
}
