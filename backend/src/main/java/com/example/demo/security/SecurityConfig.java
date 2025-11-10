package com.example.demo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    // Boot 2.7.x: 람다 DSL(requestMatchers) 대신 antMatchers 사용이 더 호환성 높음
    http
      .csrf().disable()
      .cors().and()
      .headers().frameOptions().sameOrigin().and()
      .authorizeRequests()
        .antMatchers("/", "/index.html", "/static/**", "/favicon.ico").permitAll()
        .antMatchers("/api/public/**").permitAll()
        .antMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
        .antMatchers("/uploads/**").permitAll()
        .antMatchers("/api/admin/**").hasRole("ADMIN")
        // ⬇️ 추가: 관리자만 허용 (기존 컨트롤러 경로 유지)
        .antMatchers(HttpMethod.GET, "/api/markers/requests").hasRole("ADMIN")
        .antMatchers(HttpMethod.POST,   "/api/markers/approve/**").hasRole("ADMIN")
        .antMatchers(HttpMethod.DELETE, "/api/markers/reject/**").hasRole("ADMIN")
        .anyRequest().authenticated()
      .and()
      .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
    return cfg.getAuthenticationManager();
  }
}
