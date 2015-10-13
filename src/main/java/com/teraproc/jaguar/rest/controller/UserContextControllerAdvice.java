package com.teraproc.jaguar.rest.controller;

import com.teraproc.jaguar.domain.JaguarUser;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
public class UserContextControllerAdvice {

  @ModelAttribute("user")
  public JaguarUser getRoles(HttpServletRequest request) {
    return (JaguarUser) request.getAttribute("user");
  }
}
