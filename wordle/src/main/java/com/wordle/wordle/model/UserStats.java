package com.wordle.wordle.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "user_stats")
public class UserStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false)
    private int played = 0;

    @Column(nullable = false)
    private int won = 0;

    @Column(name = "current_streak", nullable = false)
    private int currentStreak = 0;

    @Column(name = "max_streak", nullable = false)
    private int maxStreak = 0;

    @Column(nullable = false, length = 40)
    private String distribution = "0,0,0,0,0,0";

    @Column(name = "last_game_date")
    private LocalDate lastGameDate;

    @Column(name = "last_win_date")
    private LocalDate lastWinDate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public int getPlayed() { return played; }
    public void setPlayed(int played) { this.played = played; }

    public int getWon() { return won; }
    public void setWon(int won) { this.won = won; }

    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }

    public int getMaxStreak() { return maxStreak; }
    public void setMaxStreak(int maxStreak) { this.maxStreak = maxStreak; }

    public String getDistribution() { return distribution; }
    public void setDistribution(String distribution) { this.distribution = distribution; }

    public LocalDate getLastGameDate() { return lastGameDate; }
    public void setLastGameDate(LocalDate lastGameDate) { this.lastGameDate = lastGameDate; }

    public LocalDate getLastWinDate() { return lastWinDate; }
    public void setLastWinDate(LocalDate lastWinDate) { this.lastWinDate = lastWinDate; }

    public int[] distributionArray() {
        String[] parts = distribution.split(",");
        int[] arr = new int[6];
        for (int i = 0; i < 6 && i < parts.length; i++) {
            try { arr[i] = Integer.parseInt(parts[i].trim()); } catch (NumberFormatException e) { arr[i] = 0; }
        }
        return arr;
    }

    public void setDistributionArray(int[] arr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            if (i > 0) sb.append(",");
            sb.append(i < arr.length ? arr[i] : 0);
        }
        this.distribution = sb.toString();
    }
}