package com.teraproc.jaguar.config;

import com.teraproc.jaguar.domain.Provider;
import com.teraproc.jaguar.log.JaguarLoggerFactory;
import com.teraproc.jaguar.log.Logger;
import com.teraproc.jaguar.provider.manager.ApplicationManager;
import freemarker.template.TemplateException;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.quartz.simpl.SimpleJobFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@EnableScheduling
public class AppConfig implements AsyncConfigurer {

  private static final Logger LOGGER =
      JaguarLoggerFactory.getLogger(AppConfig.class);

  @Value("${jaguar.threadpool.core.size:50}")
  private int corePoolSize;
  @Value("${jaguar.threadpool.max.size:500}")
  private int maxPoolSize;
  @Value("${jaguar.threadpool.queue.size:1000}")
  private int queueCapacity;

  @Autowired
  private List<ApplicationManager> applicationManagerList;

  @Bean
  public Map<Provider, ApplicationManager> applicationManagers() {
    Map<Provider, ApplicationManager> map = new HashMap<>();
    for (ApplicationManager applicationManager : applicationManagerList) {
      map.put(applicationManager.getProvider(), applicationManager);
    }
    return map;
  }

  @Bean
  public ThreadPoolExecutorFactoryBean getThreadPoolExecutorFactoryBean() {
    ThreadPoolExecutorFactoryBean executorFactoryBean =
        new ThreadPoolExecutorFactoryBean();
    executorFactoryBean.setCorePoolSize(corePoolSize);
    executorFactoryBean.setMaxPoolSize(maxPoolSize);
    executorFactoryBean.setQueueCapacity(queueCapacity);
    return executorFactoryBean;
  }

  @Bean
  public SchedulerFactoryBean schedulerFactoryBean() {
    SchedulerFactoryBean scheduler = new SchedulerFactoryBean();
    scheduler.setTaskExecutor(getAsyncExecutor());
    scheduler.setAutoStartup(true);
    scheduler.setJobFactory(new SimpleJobFactory());
    return scheduler;
  }

  @Bean
  public RestOperations createRestTemplate() throws Exception {
    HttpComponentsClientHttpRequestFactory requestFactory =
        new HttpComponentsClientHttpRequestFactory();
    SSLContextBuilder sslContextBuilder = new SSLContextBuilder();
    sslContextBuilder.loadTrustMaterial(
        null, new TrustStrategy() {
          @Override
          public boolean isTrusted(X509Certificate[] chain, String authType)
              throws CertificateException {
            return true;
          }
        });
    SSLConnectionSocketFactory sslConnectionSocketFactory =
        new SSLConnectionSocketFactory(sslContextBuilder.build());
    CloseableHttpClient httpClient =
        HttpClients.custom().setSSLSocketFactory(sslConnectionSocketFactory)
            .build();
    requestFactory.setHttpClient(httpClient);
    return new RestTemplate(requestFactory);
  }

  @Bean
  public freemarker.template.Configuration freemarkerConfiguration()
      throws IOException, TemplateException {
    FreeMarkerConfigurationFactoryBean factoryBean =
        new FreeMarkerConfigurationFactoryBean();
    factoryBean.setPreferFileSystemAccess(false);
    factoryBean.setTemplateLoaderPath("classpath:/");
    factoryBean.afterPropertiesSet();
    return factoryBean.getObject();
  }

  @Override
  public Executor getAsyncExecutor() {
    try {
      return getThreadPoolExecutorFactoryBean().getObject();
    } catch (Exception e) {
      LOGGER.error(
          Logger.NOT_SERVICE_RELATED, "Error creating task executor.", e);
    }
    return null;
  }
}
