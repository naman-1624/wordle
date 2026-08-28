package com.wordle.wordle.service;

import com.wordle.wordle.exception.GameException;
import com.wordle.wordle.model.User;
import com.wordle.wordle.model.UserStats;
import com.wordle.wordle.repository.UserRepository;
import com.wordle.wordle.repository.UserStatsRepository;
import com.wordle.wordle.security.JwtPrincipal;
import com.wordle.wordle.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserStatsRepository statsRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final String adminUsername;

    public AuthService(UserRepository userRepository,
                       UserStatsRepository statsRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       @Value("${wordle.admin-username}") String adminUsername) {
        this.userRepository = userRepository;
        this.statsRepository = statsRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.adminUsername = adminUsername;
    }

    @Transactional
    public JwtPrincipal register(String username, String password) {
        if (username == null || password == null ||
                username.isBlank() || password.isBlank()) {
            throw new GameException(HttpStatus.BAD_REQUEST, "Username and password are required");
        }
        if (username.length() < 3 || username.length() > 30) {
            throw new GameException(HttpStatus.BAD_REQUEST, "Username must be 3-30 characters");
        }
        if (password.length() < 6) {
            throw new GameException(HttpStatus.BAD_REQUEST, "Password must be at least 6 characters");
        }
        if (userRepository.findByUsername(username).isPresent()) {
            throw new GameException(HttpStatus.CONFLICT, "Username already exists");
        }

        String role = username.trim().equalsIgnoreCase(adminUsername) ? "ADMIN" : "USER";
        User user = new User(username.trim(), passwordEncoder.encode(password), role);
        user = userRepository.save(user);

        UserStats stats = new UserStats();
        stats.setUserId(user.getId());
        statsRepository.save(stats);

        return new JwtPrincipal(user.getId(), user.getUsername(), user.getRole());
    }

    public JwtPrincipal login(String username, String password) {
        if (username == null || password == null || username.isBlank() || password.isBlank()) {
            throw new GameException(HttpStatus.BAD_REQUEST, "Username and password are required");
        }
        Optional<User> found = userRepository.findByUsername(username.trim());
        if (found.isEmpty() || !passwordEncoder.matches(password, found.get().getPassword())) {
            throw new GameException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        User user = found.get();
        return new JwtPrincipal(user.getId(), user.getUsername(), user.getRole());
    }

    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new GameException(HttpStatus.NOT_FOUND, "User not found"));
    }
}