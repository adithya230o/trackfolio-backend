package com.adithya.trackfolio.service;

import com.adithya.trackfolio.dto.AuthRequest;
import com.adithya.trackfolio.dto.AuthResponse;
import com.adithya.trackfolio.dto.RegisterRequest;
import com.adithya.trackfolio.entity.User;
import com.adithya.trackfolio.repository.UserRepository;
import com.adithya.trackfolio.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Handles authentication logic
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;

    private final Pattern gmailPattern = Pattern.compile("^[a-zA-Z0-9._%+-]+@gmail\\.com$");

    /**
     * Handles user registration
     *
     * @param request (registration data)
     * @return access and refresh tokens
     */
    public AuthResponse register(RegisterRequest request) {

        // Validate email format
        if (!gmailPattern.matcher(request.getEmail()).matches()) {
            log.warn("Invalid email address");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please enter a valid Gmail address.");
        }

        // Check for existing user
        if (repo.findByEmail(request.getEmail()).isPresent()) {
            log.warn("Email already exists");
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This email is already registered.");
        }

        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            log.warn("Empty or blank password");
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Please enter a valid password");
        }

        //Hash password
        String encodedPswd = encoder.encode(request.getPassword());
        String accessToken = jwtUtil.generateToken(request.getEmail(), false);
        String refreshToken = jwtUtil.generateToken(request.getEmail(), true);

        User user = User.builder()
                .email(request.getEmail())
                .name(request.getName())
                .password(encodedPswd)
                .refreshToken(refreshToken)
                .build();

        repo.save(user);
        log.info("Details of {} saved to db. Tokens returned.", user.getEmail());
        return new AuthResponse(accessToken, refreshToken);
    }

    /**
     * Handles user login
     *
     * @param request (login data)
     * @return access and refresh tokens
     */
    public AuthResponse login(AuthRequest request) {

        //check if user exist
        Optional<User> optionalUser = repo.findByEmail(request.getEmail());

        if (optionalUser.isEmpty()) {
            log.warn("Login failed: No user found with email {}", request.getEmail());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No user found");
        }

        User user = optionalUser.get();

        //validate password
        if (!encoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Invalid password");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid password");
        }

        String accessToken = jwtUtil.generateToken(user.getEmail(), false);
        String refreshToken = jwtUtil.generateToken(user.getEmail(), true);

        user.setRefreshToken(refreshToken);
        repo.save(user);
        log.info("User email : {} logged in. Tokens returned.", user.getEmail());

        return new AuthResponse(accessToken, refreshToken);
    }
}