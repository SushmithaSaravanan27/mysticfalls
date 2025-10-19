package com.amazecare.security;

import com.amazecare.entity.User;
import com.amazecare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String cleanUsername = username.trim(); // ✅ remove spaces
        System.out.println("Trying to load user: " + cleanUsername);

        User user = userRepository.findByUsername(cleanUsername)
                .orElseThrow(() -> {
                    System.err.println("User not found in DB: " + cleanUsername);
                    return new UsernameNotFoundException("User not found");
                });

        // ✅ Make sure role is "ROLE_ADMIN", "ROLE_PATIENT", etc.
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole().toUpperCase()))
        );
    }
}
