package com.teraproc.jaguar.provider.manager;

public class ActionException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  /**
   * Construct the exception with a message
   *
   * @param message for the exception
   */
  public ActionException(String message) {
    super(message);
  }

  /**
   * Construct the exception with a message and a cause
   *
   * @param message for the exception
   * @param cause   of the exception
   */
  public ActionException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Construct the exception with a cause
   *
   * @param cause of the exception
   */
  public ActionException(Throwable cause) {
    super(cause);
  }
}
