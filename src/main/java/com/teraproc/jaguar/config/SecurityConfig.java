package com.teraproc.jaguar.config;

import com.teraproc.jaguar.domain.JaguarUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders
    .AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration
    .WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet
    .configuration.EnableWebMvcSecurity;
import org.springframework.security.web.authentication
    .UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
@EnableWebMvcSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    // TODO: authorization
    // now permit all requests
    http.csrf().disable()
        .addFilterAfter(
            new MyRequestFilter(), UsernamePasswordAuthenticationFilter.class)
        .authorizeRequests()
        .antMatchers("/**").permitAll()
        .anyRequest().authenticated();
  }

  @Autowired
  public void configureGlobal(AuthenticationManagerBuilder auth)
      throws Exception {
    // TODO: authentication
    // now permit all users
    auth.inMemoryAuthentication()
        .withUser("admin").password("admin").roles("USER");
  }

  private static class MyRequestFilter extends OncePerRequestFilter {
    @SuppressWarnings("unchecked")
    @Override
    protected void doFilterInternal(
        HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {
      String username = request.getHeader("username");
      JaguarUser user = new JaguarUser(username, "user@example.com", "admin");
      request.setAttribute("user", user);
      filterChain.doFilter(request, response);
    }
  }
}

