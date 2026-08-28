package com.wordle.wordle.dto;

public record GameHistoryView(Long gameId, String mode, String packName,
                              String word, int attemptsUsed, boolean won,
                              int durationSeconds, String playedAt) {}