package com.teraproc.jaguar.provider.metrics;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.elasticsearch.common.io.FileSystemUtils;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ArrayList;

public class ElasticsearchClientProviderTest {
  private static final String HTTP_BASE_URL = "http://localhost";
  private static final String HTTP_PORT = "9200";
  private static final String HTTP_TRANSPORT_PORT = "9300";
  private static String index = "hbase";
  private static String type = "HBASE_REGIONSERVER";
  private static final String elasticSearchBaseUrl =
      HTTP_BASE_URL + ":" + HTTP_PORT;
  private static final String elasticSearchUrl =
      HTTP_BASE_URL + ":" + HTTP_PORT + "/" + index + "/" + type;
  private static final List<String> containers = Arrays.asList(
      new String[]{"container-1", "container-2", "container-3", "container-4"});

  private static ObjectMapper mapper;
  private static Node node;
  private HttpClient httpClient;
  private ElasticsearchClientProvider underTest;
  private long time_1;
  private long time_2;

  private static void startElasticSearch() throws Exception {
    final String nodeName = "esnode";

    Map settingsMap = new HashMap();

    // create all data directories under Maven build directory
    settingsMap.put("path.data", "target");
    settingsMap.put("path.work", "target");
    settingsMap.put("path.logs", "target");

    // set ports used by Elastic Search to something different than default
    settingsMap.put("http.port", HTTP_PORT);
    settingsMap.put("transport.tcp.port", HTTP_TRANSPORT_PORT);
    settingsMap.put("index.number_of_shards", "1");
    settingsMap.put("index.number_of_replicas", "0");

    // disable clustering
    settingsMap.put("discovery.zen.ping.multicast.enabled", "false");

    // enable automatic index creation
    settingsMap.put("action.auto_create_index", "true");

    // enable automatic type creation
    settingsMap.put("index.mapper.dynamic", "true");

    removeOldDataDir("target/" + nodeName);

    Settings settings = ImmutableSettings.settingsBuilder()
        .put(settingsMap).build();
    node = nodeBuilder().settings(settings).clusterName(nodeName)
        .local(true).node();
    node.start();
    Thread.sleep(5000);
  }

  private static void removeOldDataDir(String datadir) throws Exception {
    File dataDir = new File(datadir);
    if (dataDir.exists()) {
      FileSystemUtils.deleteRecursively(dataDir, true);
    }
  }

  @Before
  public void setup() throws Exception {
    // start an embedded elasticsearch node
    startElasticSearch();

    underTest = new ElasticsearchClientProvider();
    underTest.setElasticsearchUrl(elasticSearchBaseUrl);

    // create httpClient for test
    httpClient = new HttpClient();
    mapper = new ObjectMapper();

    // create collected metric values
    // 2 hosts, 2 containers, 4 metrics, 5 values of each metric
    for (int i = 0; i < 5; i++) {
      for (String container : containers) {
        ElasticsearchMetric metrics = new ElasticsearchMetric();
        metrics.setUpdateTime(System.currentTimeMillis());
        metrics.setAppName(index);
        metrics.setCmptName(type);
        metrics.setHostName("localhost");
        metrics.setContainerId(container);
        metrics.setContext("metrics");
        metrics.setMetrics(new HashMap<String, String>());
        metrics.getMetrics()
            .put("ProcessCallTime_mean", "50" + String.valueOf(i));
        metrics.getMetrics()
            .put("ProcessCallTime_num_ops", "100" + String.valueOf(i));
        metrics.getMetrics()
            .put("QueueCallTime_mean", "20" + String.valueOf(i));
        metrics.getMetrics()
            .put("QueueCallTime_num_ops", "100" + String.valueOf(i));

        String jsonData = mapper.writeValueAsString(metrics);
        StringRequestEntity requestEntity =
            new StringRequestEntity(jsonData, "application/json", "UTF-8");
        PostMethod postMethod = new PostMethod(elasticSearchUrl);
        postMethod.setRequestEntity(requestEntity);
        int statusCode = httpClient.executeMethod(postMethod);
        // 2xx means successful
        assertEquals(true, statusCode >= 200 && statusCode < 300);

        if (i == 3 && container.endsWith("container-1")) {
          time_1 = metrics.getUpdateTime();
        } else if (i == 4 && container.endsWith("container-1")) {
          time_2 = metrics.getUpdateTime();
        }
        Thread.sleep(100);
      }
    }
    Thread.sleep(1000);
  }

  @After
  public void stopElasticSearch() {
    node.close();
  }

  @Test
  public void TestGetInstanceMetrics() throws Exception {
    List<String> metrics = new ArrayList();
    metrics.add("ProcessCallTime_mean");
    metrics.add("ProcessCallTime_num_ops");
    assertNotNull(
        underTest.getInstanceMetrics(
            index, type, metrics, time_1, time_2));

    metrics.clear();
    metrics.add("ProcessCallTime_mean");
    metrics.add("ProcessCallTime_num_ops");
    metrics.add("QueueCallTime_mean");
    metrics.add("QueueCallTime_num_ops");
    // < containerId, < metricName, < metricValues >>>
    Map<String, Map<String, List<Number>>> allMetrics =
        underTest.getInstanceMetrics(
            index, type, metrics, time_1, time_2);
    assertEquals(4, allMetrics.size());
    assertEquals(4, allMetrics.get("container-1").size());
    assertEquals(
        2, allMetrics.get("container-1").get("ProcessCallTime_mean").size());

    allMetrics = underTest.getInstanceMetrics(
        index, type, Arrays.asList(
            new String[]{"ProcessCallTime_mean", "ProcessCallTime_num_ops"}),
        time_2, System.currentTimeMillis());
    assertEquals(4, allMetrics.size());
    assertEquals(2, allMetrics.get("container-2").size());
    assertEquals(
        1, allMetrics.get("container-2").get("ProcessCallTime_mean").size());

    // < metricName, < containerId, metricValue>>
    Map<String, Map<String, Number>> result = underTest
      .getMetricsLatestValue(
        index, type, Arrays.asList(
          new String[]{"ProcessCallTime_mean",
            "ProcessCallTime_num_ops", "QueueCallTime_mean"}), time_1, time_2);
    assertEquals(3, result.size());
    assertEquals(4, result.get("ProcessCallTime_mean").size());
    assertEquals(
      504.0,
        result.get("ProcessCallTime_mean").get("container-1").doubleValue(),
          0.0001);
  }
}
