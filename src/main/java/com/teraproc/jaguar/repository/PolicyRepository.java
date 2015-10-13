package com.teraproc.jaguar.repository;

import com.teraproc.jaguar.domain.Policy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PolicyRepository extends CrudRepository<Policy, Long> {

  Policy findByApplication(
      @Param("applicationId") Long applicationId,
      @Param("policyId") Long policyId);

  List<Policy> findAllByApplication(@Param("id") Long id);
}
