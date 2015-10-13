package com.teraproc.jaguar.service;

import com.teraproc.jaguar.domain.History;
import com.teraproc.jaguar.domain.JaguarUser;
import com.teraproc.jaguar.domain.Policy;
import com.teraproc.jaguar.repository.HistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Properties;

@Service
public class HistoryService {

  @Autowired
  private HistoryRepository historyRepository;
  @Autowired
  private ApplicationService applicationService;

  public void createEntry(
      Policy policy, String actionDef, Properties actionProps) {
    History history = new History()
        .withPolicy(policy)
        .withAction(actionDef)
        .withActionResult(actionProps);
    historyRepository.save(history);
  }

  public List<History> getHistories(JaguarUser user, long applicationId) {
    applicationService.findOneByUser(user, applicationId);
    return historyRepository.findAllByApplication(applicationId);
  }

  public History getHistory(
      JaguarUser user, long applicationId, long historyId) {
    applicationService.findOneByUser(user, applicationId);
    return historyRepository.findByApplication(applicationId, historyId);
  }
}
