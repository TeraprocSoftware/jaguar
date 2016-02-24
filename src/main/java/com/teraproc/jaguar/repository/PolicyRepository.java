package com.teraproc.jaguar.repository;

import com.teraproc.jaguar.domain.Policy;
import com.teraproc.jaguar.domain.Scope;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PolicyRepository extends CrudRepository<Policy, Long> {

  Policy findByApplication(
      @Param("applicationId") Long applicationId,
      @Param("policyId") Long policyId);

  Policy findByScope(
      @Param("applicationId") Long applicationId,
      @Param("policyId") Long policyId,
      @Param("scope") Scope scope);

  List<Policy> findAllByApplication(@Param("id") Long id);

  List<Policy> findAllByScope(@Param("id") Long id,
                              @Param("scope") Scope scope);

}
