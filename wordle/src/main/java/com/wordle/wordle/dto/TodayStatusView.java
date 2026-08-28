package com.wordle.wordle.dto;

import java.util.Map;

public record TodayStatusView(Map<String, Boolean> playedToday, GameView currentGame) {}