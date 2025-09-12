package com.adithya.trackfolio.service;

import com.adithya.trackfolio.dto.AuthRequest;
import com.adithya.trackfolio.dto.AuthResponse;
import com.adithya.trackfolio.dto.RegisterRequest;
import com.adithya.trackfolio.entity.DriveSummary;
import com.adithya.trackfolio.entity.User;
import com.adithya.trackfolio.repository.*;
import com.adithya.trackfolio.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
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
    private final UserRepository userRepository;
    private final DriveRepository driveRepository;
    private final NoteRepository noteRepository;
    private final ChecklistRepository checklistRepository;
    private final JDRepository jdRepository;
    private final SkillRepository skillRepository;
    private final ChatService chatService;

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

        // prewarm call executed asynchronously
        new Thread(() -> {
            try {
                String prewarmResponse = chatService.prewarm();
                log.info("Prewarm completed: {}", prewarmResponse);
            } catch (Exception e) {
                log.error("Prewarm failed", e);
            }
        }).start();

        String userName = user.getName();
        return new AuthResponse(accessToken, refreshToken, userName);
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

        // prewarm call executed asynchronously
        new Thread(() -> {
            try {
                String prewarmResponse = chatService.prewarm();
                log.info("Prewarm completed: {}", prewarmResponse);
            } catch (Exception e) {
                log.error("Prewarm failed", e);
            }
        }).start();

        String userName = user.getName();
        return new AuthResponse(accessToken, refreshToken, userName);
    }

    /**
     * Handles regeneration of access token
     *
     * @param refreshToken : refresh token
     * @return new access token
     */
    public AuthResponse generateAccessToken(String refreshToken) {

        String email = jwtUtil.extractEmail(refreshToken); // throws on failure

        User user = repo.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found for extracted email address : {}", email);
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token. Please login again");
                });

        if (!jwtUtil.validateToken(refreshToken)) {
            log.warn("Token validation failed");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session expired. Please login again");
        }

        if (!refreshToken.equals(user.getRefreshToken())) {
            log.warn("Submitted token doesnt match with user's token");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token. Please login again");
        }

        String newAccessToken = jwtUtil.generateToken(user.getEmail(), false);
        log.info("New accessToken generated and returned");

        // prewarm call executed asynchronously
        new Thread(() -> {
            try {
                String prewarmResponse = chatService.prewarm();
                log.info("Prewarm completed: {}", prewarmResponse);
            } catch (Exception e) {
                log.error("Prewarm failed", e);
            }
        }).start();

        String userName = user.getName();
        return new AuthResponse(newAccessToken, refreshToken, userName);
    }

    private Long getUserIdFromContext() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return repo.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"))
                .getId();
    }

    /**
     * Deletes the current user's account along with all associated data,
     * including drives, job descriptions, notes, checklists, and skills.
     * This operation is transactional; all deletions succeed or none are applied.
     */
    @Transactional
    public void deleteAccount() {
        Long userId = getUserIdFromContext();

        // 1. Delete skills
        skillRepository.deleteByUserId(userId);

        // 2. Get all drives
        List<DriveSummary> drives = driveRepository.findByUserId(userId);

        for (DriveSummary drive : drives) {
            Long driveId = drive.getId();

            // 2a. Delete JD if exists
            jdRepository.findByDriveId(driveId).ifPresent(jd -> jdRepository.delete(jd));

            // 2b. Delete notes
            noteRepository.deleteByDriveId(driveId);

            // 2c. Delete checklist items
            checklistRepository.deleteByDriveId(driveId);
        }

        // 3. Delete drives
        driveRepository.deleteByUserId(userId);

        // 4. Delete user
        userRepository.deleteById(userId);
    }
}