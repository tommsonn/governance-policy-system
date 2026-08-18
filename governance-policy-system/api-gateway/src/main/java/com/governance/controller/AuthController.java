package com.governance.controller;

import com.governance.dto.LoginRequest;
import com.governance.dto.LoginResponse;
import com.governance.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    private static final Map<String, String> USERS = Map.of(
            "admin", "$2a$12$aDqupRxq8TnwBBqlPNg/Eul0pv.cMNmM34xgle1/U09mkVN5uKd6O",// password: admin123
            "user", "$2a$12$yNTqkDWkI7xzN3M9Eqlm4.Z5ZME3QiVO7Uiuimh9aAwk2rj1/Fabu" // password: user123
    );

    private static final Map<String, String> ROLES = Map.of(
            "admin", "ADMIN",
            "user", "USER"
    );

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsername());
        String username = request.getUsername();
        String password = request.getPassword();

        if (!USERS.containsKey(username)) {
            log.warn("User not found: {}", username);
            return ResponseEntity.status(401).build();
        }

        if (!passwordEncoder.matches(password, USERS.get(username))) {
            log.warn("Invalid password for user: {}", username);
            return ResponseEntity.status(401).build();
        }

        String role = ROLES.getOrDefault(username, "USER");
        Long userId = 1L;

        String token = jwtUtil.generateToken(username, role, userId);
        String refreshToken = jwtUtil.generateToken(username, role, userId);

        log.info("Login successful for user: {}", username);
        return ResponseEntity.ok(new LoginResponse(token, refreshToken, userId, username, role));
    }
}