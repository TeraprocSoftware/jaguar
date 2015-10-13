package com.teraproc.jaguar.repository;

import com.teraproc.jaguar.domain.Application;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ApplicationRepository
    extends CrudRepository<Application, Long> {

  Application find(@Param("id") Long id);

  List<Application> findAllByUser(@Param("id") String id);

  List<Application> findAll();

  List<Application> findAllByState(@Param("enabled") boolean enabled);
}
