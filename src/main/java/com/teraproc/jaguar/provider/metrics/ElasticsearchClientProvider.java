package com.teraproc.jaguar.provider.metrics;

import com.teraproc.jaguar.log.JaguarLoggerFactory;
import com.teraproc.jaguar.log.Logger;
import com.teraproc.jaguar.utils.UnableToConnectException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Service
public class ElasticsearchClientProvider {
  private static final Logger LOGGER =
      JaguarLoggerFactory.getLogger(ElasticsearchClientProvider.class);
  private static ObjectMapper mapper = new ObjectMapper();
  private static final String METRICS_PREFIX = "metrics.";
  private static final String METRIC_CONTAINERID = "containerId";
  private static final String METRIC_UPDATE_TINME = "updateTime";

  @Value("${metrics.elasticsearch.url}")
  private String elasticsearchUrl;

  private HttpClient httpClient =
      new HttpClient(new MultiThreadedHttpConnectionManager());

  public Map<String, Map<String, List<Number>>> getInstanceMetrics(
      String service
      , String component, List<String> metrics, long from, long to)
      throws Exception {
    // < containerId, < metricName, < metricValues >>>
    Map<String, Map<String, List<Number>>> result = new HashMap<>();
    String jsonRequest =
        mapper.writeValueAsString(constructRequest(metrics, from, to));
    String jsonResponse = getMetrics(service, component, jsonRequest);
    ElasticsearchMetricResponse response =
        mapper.readValue(jsonResponse, ElasticsearchMetricResponse.class);
    if (response.getHits() != null && response.getHits().getHits() != null
        && response.getHits().getHits().iterator().next().getFields() != null) {
      for (ElasticsearchHitsHits hit : response.getHits().getHits()) {
        String containerId =
            (String) hit.getFields().get(METRIC_CONTAINERID).toArray()[0];
        for (Map.Entry<String, Collection<String>> entry : hit.getFields()
            .entrySet()) {
          String metricName = entry.getKey();
          if (metricName.equals(METRIC_CONTAINERID)) {
            continue;
          }

          final Number metricValue =
              Float.valueOf((String) entry.getValue().toArray()[0]);
          metricName = metricName.substring(METRICS_PREFIX.length());
          if (!result.containsKey(containerId)) {
            result.put(containerId, new HashMap<String, List<Number>>());
          }
          if (!result.get(containerId).containsKey(metricName)) {
            result.get(containerId).put(metricName, new ArrayList<Number>());
          }
          result.get(containerId).get(metricName).add(metricValue);
        }
      }
    }
    return result;
  }

  public Map<String, Map<String, Number>> getMetricsLatestValue(
      String service, String component, List<String> metrics, long from,
      long to)
      throws Exception {
    // < metricName, < containerId, metricValue>>
    Map<String, Map<String, Number>> result = new HashMap<>();
    String jsonRequest =
        mapper.writeValueAsString(constructRequest(metrics, from, to));
    String jsonResponse = getMetrics(service, component, jsonRequest);
    ElasticsearchMetricResponse response =
        mapper.readValue(jsonResponse, ElasticsearchMetricResponse.class);
    if (response.getHits() != null && response.getHits().getHits() != null
        && response.getHits().getHits().iterator().next().getFields() != null) {
      for (ElasticsearchHitsHits hit : response.getHits().getHits()) {
        String containerId =
            (String) hit.getFields().get(METRIC_CONTAINERID).toArray()[0];
        for (Map.Entry<String, Collection<String>> entry : hit.getFields()
            .entrySet()) {
          String metricName = entry.getKey();
          if (metricName.equals(METRIC_CONTAINERID)) {
            continue;
          }

          final Number metricValue =
              Float.valueOf((String) entry.getValue().toArray()[0]);
          metricName = metricName.substring(METRICS_PREFIX.length());
          if (!result.containsKey(metricName)) {
            result.put(metricName, new HashMap<String, Number>());
          }
          result.get(metricName).put(containerId, metricValue);
        }
      }
    }
    return result;
  }


  /** Query request look like:
      {
      "fields" : ["metrics.$metric_1", "metrics.metric_2", "containerId"],
      "query" : {
        "filtered" : {
          "filter" : {
            "or" : [{
                "exists" : {
                  "field" : "metrics.$metric_1"
                }
              }, {
                "exists" : {
                  "field" : "metrics.$metric_2"
                }
              }
            ]
          },
          "query" : {
            "range" : {
              "updateTime" : {
                "to" : "$now",
                "from" : "$lastQueryTime"
              }
            }
          }
        }
      },
      "sort": {
        "updateTime": {
          "order": "asc"
        }
      }
    }
   */
  private ElasticsearchMetricsRequest constructRequest(
      List<String> metrics, long from, long to) {
    ElasticsearchMetricsRequest request = new ElasticsearchMetricsRequest();
    // fields
    request.setFields(convertMetricsToFields(metrics));
    // query
    request.setQuery(
        new Query(
            new Filtered(
                convertMetricsToFilter(metrics),
                getRangeQuery(from, to))));
    // asc sort by metrics updateTime
    request.setSort(getSort());
    return request;
  }

  private List<String> convertMetricsToFields(
      List<String> metrics) {
    // Add prefix; remove it in future?
    List<String> fields = new ArrayList<String>();
    fields.add(METRIC_CONTAINERID);
    for (String metricName : metrics) {
      fields.add(METRICS_PREFIX + metricName);
    }
    return fields;
  }

  private Filter convertMetricsToFilter(List<String> metrics) {
    List<ExistsEntity> orCollection = new ArrayList<>();
    for (String metric : metrics) {
      FieldEntity field = new FieldEntity();
      field.setField(metric);
      ExistsEntity existFilter = new ExistsEntity();
      existFilter.setExists(field);
      orCollection.add(existFilter);
    }
    Filter filter = new Filter();
    filter.setOr(orCollection);
    return filter;
  }

  private Range getRangeQuery(long from, long to) {
    Range range = new Range();
    range.getRange()
        .put(METRIC_UPDATE_TINME, new TimeRange(from, to));
    return range;
  }

  private Map<String, SortEntity> getSort() {
    Map<String, SortEntity> sort = new HashMap<>();
    sort.put(METRIC_UPDATE_TINME, new SortEntity());
    return sort;
  }

  private String getMetrics(String index, String type, String jsonRequest) {
    String queryUrl =
        getElasticsearchUrl() + "/" + index + "/" + type + "/_search?";
    LOGGER.debug(
        Logger.NOT_SERVICE_RELATED,
        "Query metrics from elasticsearch url: " + queryUrl);
    LOGGER.debug(
        Logger.NOT_SERVICE_RELATED, "Query metrics request: " + jsonRequest);

    HttpGetWithEntity getMethod = new HttpGetWithEntity(queryUrl);
    try {
      StringRequestEntity requestEntity =
          new StringRequestEntity(jsonRequest, "application/json", "UTF-8");
      getMethod.setRequestEntity(requestEntity);
      int statusCode = httpClient.executeMethod(getMethod);
      if (statusCode != 200) {
        LOGGER.info(
            Logger.NOT_SERVICE_RELATED,
            "Unable to GET metrics from elasticsearch, " + queryUrl);
      } else {
        LOGGER.debug(
            Logger.NOT_SERVICE_RELATED,
            "Metrics get from elasticsearch " + getMethod
                .getResponseBodyAsString());
      }
      return getMethod.getResponseBodyAsString();
    } catch (Exception e) {
      throw new UnableToConnectException(e).setConnectUrl(queryUrl);
    } finally {
      getMethod.releaseConnection();
    }
  }

  public String getElasticsearchUrl() {
    return elasticsearchUrl;
  }

  public void setElasticsearchUrl(String url) {
    elasticsearchUrl = url;
  }
}
