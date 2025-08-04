package com.adithya.trackfolio.security;

import com.adithya.trackfolio.entity.User;
import com.adithya.trackfolio.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT-based authentication filter that intercepts incoming HTTP requests,
 * validates the JWT token, and sets the authentication context accordingly.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository repo;

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {

        String path = req.getRequestURI();
        if (path.startsWith("/auth")) {
            chain.doFilter(req, response);
            return;
        }

        String authHeader = req.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("JwtFilter : Missing or malformed Authorization header");
            sendUnauthorized(response, "Access token expired");
            return;
        }

        String token = authHeader.substring(7);
        String email;

        try {
            // 1. Extract email from token
            email = jwtUtil.tryExtractEmail(token);

            // 2. Validate token signature and expiration
            if (!jwtUtil.validateToken(token)) {
                log.warn("JwtFilter : Invalid JWT token");
                sendUnauthorized(response, "Access token expired");
                return;
            }
        } catch (ExpiredJwtException e) {
            log.warn("JwtFilter : Token expired");
            sendUnauthorized(response, "Access token expired");
            return;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JwtFilter : Failed to extract email from token. Error: {}", e.getMessage());
            sendUnauthorized(response, "Access token expired");
            return;
        }

        // 3. Only set security context if not already set
        // Another filter (like session-based auth or OAuth) might have already set it
        if (SecurityContextHolder.getContext().getAuthentication() == null) {

            // 4. Ensure user exists in DB (token might be valid but user deleted/deactivated)
            User user = repo.findByEmail(email).orElse(null);
            if (user == null) {
                log.warn("JwtFilter : User not found for the given token");
                sendUnauthorized(response, "Access token expired");
                return;
            }

            // 5. Build userDetails using email only (roles not used in current auth model) for Security Context
            UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                    user.getEmail(),
                    "", // password not needed
                    Collections.emptyList()
            );

            // 6. Set that user as authenticated for the current request/thread
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        // Continue with the filter chain -> Controller
        chain.doFilter(req, response);
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        if (response.isCommitted()) {
            log.error("JwtFilter: Response already committed — cannot write body");
            return;
        }

        log.warn("JwtFilter: Sending 401 with message: {}", message);

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        String json = String.format("""
                {
                  "timestamp": "%s",
                  "status": 401,
                  "error": "Unauthorized",
                  "message": "%s"
                }
                """, java.time.Instant.now(), message);

        response.getWriter().write(json);
    }
}