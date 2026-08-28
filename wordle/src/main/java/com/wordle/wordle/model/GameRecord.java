package com.wordle.wordle.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "game_records")
public class GameRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 20)
    private String mode;

    @Column(name = "pack_id")
    private Long packId;

    @Column(nullable = false, length = 5)
    private String word;

    @Column(name = "attempts_used", nullable = false)
    private int attemptsUsed = 0;

    @Column(nullable = false)
    private boolean won = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GameStatus status = GameStatus.IN_PROGRESS;

    @Column(name = "duration_seconds", nullable = false)
    private int durationSeconds = 0;

    @Column(name = "game_date", nullable = false)
    private LocalDate gameDate = LocalDate.now();

    @Column(length = 4000)
    private String history = "";

    @Column(name = "played_at", nullable = false)
    private Instant playedAt = Instant.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public Long getPackId() { return packId; }
    public void setPackId(Long packId) { this.packId = packId; }

    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }

    public int getAttemptsUsed() { return attemptsUsed; }
    public void setAttemptsUsed(int attemptsUsed) { this.attemptsUsed = attemptsUsed; }

    public boolean isWon() { return won; }
    public void setWon(boolean won) { this.won = won; }

    public GameStatus getStatus() { return status; }
    public void setStatus(GameStatus status) { this.status = status; }

    public int getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(int durationSeconds) { this.durationSeconds = durationSeconds; }

    public LocalDate getGameDate() { return gameDate; }
    public void setGameDate(LocalDate gameDate) { this.gameDate = gameDate; }

    public String getHistory() { return history; }
    public void setHistory(String history) { this.history = history; }

    public Instant getPlayedAt() { return playedAt; }
    public void setPlayedAt(Instant playedAt) { this.playedAt = playedAt; }
}