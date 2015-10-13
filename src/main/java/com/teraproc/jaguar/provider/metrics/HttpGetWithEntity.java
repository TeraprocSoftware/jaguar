package com.teraproc.jaguar.provider.metrics;

import org.apache.commons.httpclient.methods.PostMethod;

public class HttpGetWithEntity extends PostMethod {
  public static final String METHOD_NAME = "GET";

  public HttpGetWithEntity(String url) {
    super(url);
  }

  @Override
  public String getName() {
    return METHOD_NAME;
  }
}
