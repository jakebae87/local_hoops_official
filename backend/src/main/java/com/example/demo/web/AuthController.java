package com.example.demo.web;

import com.example.demo.security.JwtTokenProvider;
import javax.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {

  private final AuthenticationManager authenticationManager;
  private final JwtTokenProvider tokenProvider;

  public AuthController(AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider) {
    this.authenticationManager = authenticationManager;
    this.tokenProvider = tokenProvider;
  }

  public static class LoginRequest {
    @NotBlank public String username;
    @NotBlank public String password;
  }
  public static class TokenResponse {
    public String token;
    public TokenResponse(String token) { this.token = token; }
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody LoginRequest req) {
    Authentication auth = authenticationManager.authenticate(
      new UsernamePasswordAuthenticationToken(req.username, req.password)
    );
    String token = tokenProvider.generateToken(auth);
    return ResponseEntity.ok(new TokenResponse(token));
  }
}
