package com.wordle.wordle.dto;

public record LeaderboardEntry(int rank, String username, int played, int won,
                               int winRate, int maxStreak, int currentStreak,
                               String avgGuesses) {}