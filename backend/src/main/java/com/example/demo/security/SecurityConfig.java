package com.example.demo.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserDetailsServiceImpl userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    public SecurityConfig(UserDetailsServiceImpl userDetailsService,
                          PasswordEncoder passwordEncoder,
                          JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    // 로그인 시 DB 사용자 + bcrypt 비밀번호 비교에 사용
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
            .userDetailsService(userDetailsService)
            .passwordEncoder(passwordEncoder);
    }

    // AuthController에서 AuthenticationManager 주입받을 때 필요
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    // URL 권한 설정
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .cors()
            .and()
            .csrf().disable()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // JWT이므로 세션 X
            .and()
            .authorizeRequests()
                // 로그인 / 회원가입은 모두 허용
                .antMatchers("/api/auth/**").permitAll()

                // 정적 리소스 (필요에 맞게 수정 가능)
                .antMatchers(
                        "/",
                        "/index.html",
                        "/static/**",
                        "/js/**",
                        "/css/**",
                        "/images/**"
                ).permitAll()

                // 마커/댓글 조회는 누구나 가능
                .antMatchers(HttpMethod.GET, "/api/markers/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/comments/**").permitAll()

                // 관리자 전용 API
                .antMatchers("/api/markers/approve/**", "/api/markers/reject/**").hasRole("ADMIN")
                .antMatchers("/api/admin/**").hasRole("ADMIN")

                // 마커 관련 쓰기 API (등록 요청, 수정, 삭제 등) → 로그인 필요
                .antMatchers(HttpMethod.POST, "/api/markers/**").authenticated()
                .antMatchers(HttpMethod.PUT, "/api/markers/**").authenticated()
                .antMatchers(HttpMethod.DELETE, "/api/markers/**").authenticated()

                // 댓글 쓰기/삭제 → 로그인 필요
                .antMatchers(HttpMethod.POST, "/api/comments/**").authenticated()
                .antMatchers(HttpMethod.DELETE, "/api/comments/**").authenticated()

                // 좋아요/싫어요 (추후 /api/votes/** 로 구현할 예정) → 로그인 필요
                .antMatchers("/api/votes/**").authenticated()

                // 그 외는 일단 허용 (필요하면 authenticated() 로 바꿔도 됨)
                .anyRequest().permitAll()
            .and()
            .exceptionHandling();

        // JWT 필터를 UsernamePasswordAuthenticationFilter 이전에 추가
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
