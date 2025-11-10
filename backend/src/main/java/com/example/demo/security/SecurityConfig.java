package com.example.demo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
      .csrf().disable()
      .cors().and()
      .authorizeRequests()
        // 로그인은 공개
        .antMatchers(HttpMethod.POST, "/api/auth/login").permitAll()

        // ✅ 지도 기본 페이지(승인된 마커 목록/상세)는 공개
        .antMatchers(HttpMethod.GET, "/api/markers/approve").permitAll()
        .antMatchers(HttpMethod.GET, "/api/markers/**").permitAll()

        // ✅ 관리자 전용: AdminView에서만 사용되는 API
        .antMatchers(HttpMethod.GET,    "/api/markers/requests").hasRole("ADMIN")
        .antMatchers(HttpMethod.POST,   "/api/markers/approve/**").hasRole("ADMIN")
        .antMatchers(HttpMethod.DELETE, "/api/markers/reject/**").hasRole("ADMIN")

        // 그 외는 인증 필요
        .anyRequest().authenticated()
      .and()
      // JWT 필터를 UsernamePasswordAuthenticationFilter 앞에 배치
      .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
    return cfg.getAuthenticationManager();
  }
}
