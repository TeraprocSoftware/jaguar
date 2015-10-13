package com.teraproc.jaguar.rest.controller;

import com.teraproc.jaguar.domain.History;
import com.teraproc.jaguar.domain.JaguarUser;
import com.teraproc.jaguar.rest.converter.HistoryConverter;
import com.teraproc.jaguar.rest.json.HistoryJson;
import com.teraproc.jaguar.service.HistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/v1/applications/{applicationId}/history")
public class HistoryController {

  @Autowired
  private HistoryService historyService;
  @Autowired
  private HistoryConverter historyConverter;

  @RequestMapping(method = RequestMethod.GET)
  public ResponseEntity<List<HistoryJson>> getHistory(
      @ModelAttribute("user") JaguarUser user,
      @PathVariable long applicationId) {
    List<History> history = historyService.getHistories(user, applicationId);
    return new ResponseEntity<>(
        historyConverter.convertAllToJson(history), HttpStatus.OK);
  }

  @RequestMapping(value = "/{historyId}", method = RequestMethod.GET)
  public ResponseEntity<HistoryJson> getHistory(
      @ModelAttribute("user") JaguarUser user, @PathVariable long applicationId,
      @PathVariable long historyId) {
    History history = historyService.getHistory(user, applicationId, historyId);
    return new ResponseEntity<>(
        historyConverter.convert(history), HttpStatus.OK);
  }
}
