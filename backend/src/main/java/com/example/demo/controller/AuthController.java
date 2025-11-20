package com.example.demo.controller;

import com.example.demo.mapper.UserMapper;
import com.example.demo.security.jwt.JwtTokenProvider;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtTokenProvider tokenProvider,
                          UserMapper userMapper,
                          PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Data
    public static class RegisterRequest {
        private String email;
        private String password;
        private String nickname;
    }

    @Data
    public static class LoginRequest {
        // 프론트에서 email을 username으로 보냄
        private String username;
        private String password;
    }

    @Data
    public static class TokenResponse {
        private String token;
        public TokenResponse(String token) {
            this.token = token;
        }
    }

    // 회원가입
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {

        if (req.getEmail() == null || req.getEmail().isEmpty() ||
            req.getPassword() == null || req.getPassword().isEmpty() ||
            req.getNickname() == null || req.getNickname().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "이메일, 비밀번호, 닉네임을 모두 입력해주세요."));
        }

        if (userMapper.existsByEmail(req.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "이미 가입된 이메일입니다."));
        }

        Map<String, Object> data = new HashMap<>();
        data.put("email", req.getEmail());
        data.put("password", passwordEncoder.encode(req.getPassword())); // 🔒 반드시 해시
        data.put("nickname", req.getNickname());
        data.put("provider", "LOCAL");
        data.put("providerId", null);
        data.put("role", "USER"); // 기본 USER, 관리자는 DB에서 role='ADMIN'으로 수동 수정

        userMapper.insertUser(data);

        return ResponseEntity.ok(Map.of("message", "회원가입이 완료되었습니다."));
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            req.getUsername(),  // email
                            req.getPassword()
                    )
            );

            String token = tokenProvider.generateToken(auth); // 기존 JwtTokenProvider 메서드명에 맞게

            return ResponseEntity.ok(new TokenResponse(token));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "아이디 또는 비밀번호가 올바르지 않습니다."));
        }
    }
}
