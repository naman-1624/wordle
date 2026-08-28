package com.wordle.wordle.dto;

import java.util.List;

public record StatsView(String username, int played, int won, int winRate,
                        int currentStreak, int maxStreak, String avgGuesses,
                        List<Integer> distribution) {}