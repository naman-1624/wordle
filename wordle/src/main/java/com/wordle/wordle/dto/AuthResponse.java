package com.wordle.wordle.dto;

public record AuthResponse(boolean success, String token, Long id, String username,
                           String role, String message) {}