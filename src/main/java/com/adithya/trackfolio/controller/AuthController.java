package com.adithya.trackfolio.controller;

import com.adithya.trackfolio.dto.AuthRequest;
import com.adithya.trackfolio.dto.AuthResponse;
import com.adithya.trackfolio.dto.RegisterRequest;
import com.adithya.trackfolio.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles user authentication endpoints
 * Supports register
 * Returns access and refresh tokens on successful authentication
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService service;

    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest request) {
        return service.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request) {
        return service.login(request);
    }
}

