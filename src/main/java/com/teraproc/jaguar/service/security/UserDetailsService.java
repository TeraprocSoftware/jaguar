package com.teraproc.jaguar.service.security;

import com.teraproc.jaguar.domain.JaguarUser;
import org.springframework.cache.annotation.Cacheable;

public interface UserDetailsService {

  @Cacheable("userCache")
  JaguarUser getDetails(String fieldValue, UserFilterField filterField);

}
