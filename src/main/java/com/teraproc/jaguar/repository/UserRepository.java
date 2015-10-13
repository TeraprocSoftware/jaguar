package com.teraproc.jaguar.repository;

import com.teraproc.jaguar.domain.JaguarUser;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<JaguarUser, String> {
}
