package com.wordle.wordle.dto;

public record AdminUserView(Long id, String username, String role, int played,
                            int won, int currentStreak, int maxStreak, String createdAt) {}