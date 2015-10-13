package com.teraproc.jaguar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = "com.teraproc.jaguar")
public class JaguarApplication {

  public static void main(String[] args) {
    if (!VersionedApplication.versionedApplication().showVersionInfo(args)) {
      if (args.length == 0) {
        SpringApplication.run(JaguarApplication.class);
      } else {
        SpringApplication.run(JaguarApplication.class, args);
      }
    }
  }

}
