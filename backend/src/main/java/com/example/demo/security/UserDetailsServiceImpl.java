package com.example.demo.security;

import com.example.demo.mapper.UserMapper;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;

    public UserDetailsServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // username = email
        Map<String, Object> user = userMapper.findByEmail(username);

        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        String password = (String) user.get("password");
        String role = (String) user.get("role"); // 'USER' or 'ADMIN'

        return User.withUsername(username)
                   .password(password)
                   .roles(role)  // ROLE_USER, ROLE_ADMIN 으로 매핑됨
                   .build();
    }
}
