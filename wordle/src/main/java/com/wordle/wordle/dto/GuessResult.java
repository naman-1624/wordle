package com.wordle.wordle.dto;

public record GuessResult(Long gameId, String guess, String result, String status,
                          int attemptsUsed, int attemptsLeft, boolean won) {}