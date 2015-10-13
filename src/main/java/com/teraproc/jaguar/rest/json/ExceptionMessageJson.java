package com.teraproc.jaguar.rest.json;

public class ExceptionMessageJson {

  private String message;

  public ExceptionMessageJson() {
  }

  public ExceptionMessageJson(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
