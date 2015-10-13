package com.teraproc.jaguar.rest.controller;

import com.teraproc.jaguar.log.JaguarLoggerFactory;
import com.teraproc.jaguar.log.Logger;
import com.teraproc.jaguar.rest.json.ExceptionMessageJson;
import com.teraproc.jaguar.service.InvalidFormatException;
import com.teraproc.jaguar.service.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;
import java.util.NoSuchElementException;

@ControllerAdvice
public class ExceptionController {

  private static final Logger LOGGER =
      JaguarLoggerFactory.getLogger(ExceptionController.class);

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ExceptionMessageJson> handleIllegalArgException(
      IllegalArgumentException e) {
    LOGGER.error(
        Logger.NOT_SERVICE_RELATED, "Unexpected illegal argument exception", e);
    return createExceptionMessage(e.getMessage());
  }

  @ExceptionHandler({NoSuchElementException.class, NotFoundException.class })
  public ResponseEntity<ExceptionMessageJson> handleNotFoundExceptions(
      Exception e) {
    LOGGER.error(Logger.NOT_SERVICE_RELATED, "Not found", e);
    String message = e.getMessage();
    return createExceptionMessage(
        message == null ? "Not found" : message, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(InvalidFormatException.class)
  public ResponseEntity<ExceptionMessageJson> handleInvalidFormatException(
      InvalidFormatException e) {
    return createExceptionMessage(e.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ExceptionMessageJson> handleInvalidArgumentException(
      MethodArgumentNotValidException error) {
    List<FieldError> errors = error.getBindingResult().getFieldErrors();
    String message = null;
    for (FieldError fieldError : errors) {
      message = message == null ? fieldError.getDefaultMessage()
          : message + "; " + fieldError.getDefaultMessage();
    }
    return createExceptionMessage(message, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ExceptionMessageJson> handleNoScalingGroupException(
      AccessDeniedException e) {
    return createExceptionMessage(e.getMessage(), HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler({Exception.class, RuntimeException.class })
  public ResponseEntity<ExceptionMessageJson> handleServerException(
      Exception e) {
    return createExceptionMessage(
        e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
  }

  private static ResponseEntity<ExceptionMessageJson> createExceptionMessage(
      String message) {
    return createExceptionMessage(message, HttpStatus.BAD_REQUEST);
  }

  private static ResponseEntity<ExceptionMessageJson> createExceptionMessage(
      String message, HttpStatus statusCode) {
    return new ResponseEntity<>(new ExceptionMessageJson(message), statusCode);
  }
}
