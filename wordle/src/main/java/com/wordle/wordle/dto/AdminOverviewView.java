package com.wordle.wordle.dto;

public record AdminOverviewView(long totalUsers, long totalGames, long gamesToday,
                                long gamesWon, int activePlayers) {}