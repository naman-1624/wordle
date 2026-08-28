package com.wordle.wordle.dto;

import java.util.List;

public record GameView(Long gameId, String mode, Long packId, String status,
                       int attemptsUsed, int attemptsLeft, int durationSeconds,
                       List<GuessView> history) {}