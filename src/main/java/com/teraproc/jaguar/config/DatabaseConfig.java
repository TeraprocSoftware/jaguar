package com.teraproc.jaguar.config;

import org.postgresql.Driver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
public class DatabaseConfig {

  @Value("${jaguar.db.user:postgres}")
  private String dbUser;

  @Value("${jaguar.db.pass:}")
  private String dbPassword;

  @Value("${jaguar.db.name:postgres}")
  private String dbName;

  @Value("${jaguar.db.tcp.addr}")
  private String dbHost;

  @Value("${jaguar.db.tcp.port}")
  private String dbPort;

  @Value("${jaguar.db.hbm2ddl.strategy:update}")
  private String hbm2ddlStrategy;

  @Bean
  public DataSource dataSource() {
    SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
    dataSource.setDriverClass(Driver.class);
    dataSource.setUrl(
        String.format("jdbc:postgresql://%s:%s/%s", dbHost, dbPort, dbName));
    dataSource.setUsername(dbUser);
    dataSource.setPassword(dbPassword);
    return dataSource;
  }

  @Bean
  public PlatformTransactionManager transactionManager() throws Exception {
    JpaTransactionManager jpaTransactionManager = new JpaTransactionManager();
    jpaTransactionManager.setEntityManagerFactory(entityManagerFactory());
    jpaTransactionManager.afterPropertiesSet();
    return jpaTransactionManager;
  }

  @Bean
  public EntityManager entityManager(
      EntityManagerFactory entityManagerFactory) {
    return entityManagerFactory.createEntityManager();
  }

  @Bean
  public EntityManagerFactory entityManagerFactory() {
    LocalContainerEntityManagerFactoryBean entityManagerFactory =
        new LocalContainerEntityManagerFactoryBean();

    entityManagerFactory.setPackagesToScan("com.teraproc.jaguar.domain");
    entityManagerFactory.setDataSource(dataSource());

    entityManagerFactory.setJpaVendorAdapter(jpaVendorAdapter());
    entityManagerFactory.setJpaProperties(jpaProperties());
    entityManagerFactory.afterPropertiesSet();
    return entityManagerFactory.getObject();
  }

  @Bean
  public JpaVendorAdapter jpaVendorAdapter() {
    HibernateJpaVendorAdapter hibernateJpaVendorAdapter =
        new HibernateJpaVendorAdapter();
    hibernateJpaVendorAdapter.setShowSql(true);
    hibernateJpaVendorAdapter.setDatabase(Database.POSTGRESQL);
    return hibernateJpaVendorAdapter;
  }

  private Properties jpaProperties() {
    Properties properties = new Properties();
    properties.setProperty("hibernate.hbm2ddl.auto", hbm2ddlStrategy);
    properties.setProperty("hibernate.show_sql", "false");
    properties.setProperty(
        "hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
    return properties;
  }

}
