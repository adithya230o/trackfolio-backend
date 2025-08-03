package com.adithya.trackfolio.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configures Spring Security
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    /**
     * Registers a PasswordEncoder bean using BCrypt.
     * Used to hash and verify user passwords securely.
     */
    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }
}