package com.teraproc.jaguar.repository;

import com.teraproc.jaguar.domain.History;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PostAuthorize;

import java.util.List;

public interface HistoryRepository extends CrudRepository<History, Long> {

  List<History> findAllByApplication(@Param("id") Long id);

  List<History> findAllByPolicy(
      @Param("applicationId") Long applicationId,
      @Param("policyId") Long policyId);

  @PostAuthorize("hasPermission(returnObject,'read')")
  History findByApplication(
      @Param("applicationId") Long applicationId,
      @Param("historyId") Long historyId);

}
