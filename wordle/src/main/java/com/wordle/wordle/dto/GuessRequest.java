package com.wordle.wordle.dto;

public record GuessRequest(String guess, Integer durationSeconds) {}