package com.wordle.wordle.controller;

import com.wordle.wordle.dto.AuthRequest;
import com.wordle.wordle.dto.AuthResponse;
import com.wordle.wordle.model.User;
import com.wordle.wordle.security.CurrentUser;
import com.wordle.wordle.security.JwtPrincipal;
import com.wordle.wordle.security.JwtUtil;
import com.wordle.wordle.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService service;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService service, JwtUtil jwtUtil) {
        this.service = service;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody AuthRequest request) {
        JwtPrincipal principal = service.register(request.username(), request.password());
        return ResponseEntity.ok(buildResponse(principal, "Account created"));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        JwtPrincipal principal = service.login(request.username(), request.password());
        return ResponseEntity.ok(buildResponse(principal, "Login successful"));
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me() {
        JwtPrincipal principal = CurrentUser.get();
        return ResponseEntity.ok(Map.of(
                "id", principal.userId(),
                "username", principal.username(),
                "role", principal.role()
        ));
    }

    private AuthResponse buildResponse(JwtPrincipal principal, String message) {
        return new AuthResponse(true, jwtUtil.generateToken(principal),
                principal.userId(), principal.username(), principal.role(), message);
    }
}