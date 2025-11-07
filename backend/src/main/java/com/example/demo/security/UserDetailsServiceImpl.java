package com.example.demo.security;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Profile("!prod") // 운영 제외(개발/테스트용)
public class UserDetailsServiceImpl implements UserDetailsService {

  private final PasswordEncoder encoder;

  public UserDetailsServiceImpl(PasswordEncoder encoder) {
    this.encoder = encoder;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    // Java 8: Map.of(...) 미지원 → HashMap 사용
    Map<String, String> users = new HashMap<String, String>();
    users.put("user", encoder.encode("user1234"));
    users.put("admin", encoder.encode("admin1234"));

    if (!users.containsKey(username)) {
      throw new UsernameNotFoundException("User not found: " + username);
    }

    if ("admin".equals(username)) {
      return User.withUsername("admin").password(users.get("admin")).roles("ADMIN").build();
    }
    return User.withUsername("user").password(users.get("user")).roles("USER").build();
  }
}
