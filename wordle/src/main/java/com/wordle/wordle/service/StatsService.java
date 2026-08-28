package com.wordle.wordle.service;

import com.wordle.wordle.dto.LeaderboardEntry;
import com.wordle.wordle.dto.StatsView;
import com.wordle.wordle.exception.GameException;
import com.wordle.wordle.model.GameRecord;
import com.wordle.wordle.model.User;
import com.wordle.wordle.model.UserStats;
import com.wordle.wordle.repository.UserRepository;
import com.wordle.wordle.repository.UserStatsRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatsService {

    private final UserStatsRepository statsRepository;
    private final UserRepository userRepository;

    public StatsService(UserStatsRepository statsRepository, UserRepository userRepository) {
        this.statsRepository = statsRepository;
        this.userRepository = userRepository;
    }

    public UserStats getOrCreate(Long userId) {
        return statsRepository.findByUserId(userId).orElseGet(() -> {
            UserStats stats = new UserStats();
            stats.setUserId(userId);
            return statsRepository.save(stats);
        });
    }

    @Transactional
    public void recordResult(GameRecord game, User user) {
        UserStats stats = getOrCreate(user.getId());
        LocalDate today = LocalDate.now();

        stats.setPlayed(stats.getPlayed() + 1);
        stats.setLastGameDate(today);

        if (game.isWon()) {
            stats.setWon(stats.getWon() + 1);
            if (today.equals(stats.getLastWinDate())) {
                // already won today -> streak unchanged
            } else if (today.minusDays(1).equals(stats.getLastWinDate())) {
                stats.setCurrentStreak(stats.getCurrentStreak() + 1);
            } else {
                stats.setCurrentStreak(1);
            }
            stats.setMaxStreak(Math.max(stats.getMaxStreak(), stats.getCurrentStreak()));
            stats.setLastWinDate(today);

            int[] dist = stats.distributionArray();
            int slot = Math.min(game.getAttemptsUsed() - 1, 5);
            if (slot >= 0) dist[slot]++;
            stats.setDistributionArray(dist);
        } else {
            // a loss only breaks the streak if the player has not already won today
            if (!today.equals(stats.getLastWinDate())) {
                stats.setCurrentStreak(0);
            }
        }

        statsRepository.save(stats);
    }

    public StatsView viewFor(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GameException(HttpStatus.NOT_FOUND, "User not found"));
        return toView(user.getUsername(), getOrCreate(userId));
    }

    private StatsView toView(String username, UserStats s) {
        int[] dist = s.distributionArray();
        List<Integer> distribution = new ArrayList<>();
        for (int v : dist) distribution.add(v);

        double avg = s.getWon() > 0
                ? (double) sumWeighted(dist) / s.getWon()
                : 0.0;
        String avgStr = avg > 0 ? String.format("%.2f", avg) : "N/A";

        int winRate = s.getPlayed() > 0 ? (int) Math.round((s.getWon() * 100.0) / s.getPlayed()) : 0;

        return new StatsView(username, s.getPlayed(), s.getWon(), winRate,
                s.getCurrentStreak(), s.getMaxStreak(), avgStr, distribution);
    }

    private int sumWeighted(int[] dist) {
        int sum = 0;
        for (int i = 0; i < 6; i++) sum += dist[i] * (i + 1);
        return sum;
    }

    public List<LeaderboardEntry> leaderboard(String sortBy) {
        List<UserStats> all = statsRepository.findAll();
        Map<Long, String> usernames = userRepository.findAll().stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));

        List<LeaderboardEntry> entries = new ArrayList<>();
        for (UserStats s : all) {
            String username = usernames.get(s.getUserId());
            if (username == null) continue;
            int winRate = s.getPlayed() > 0 ? (int) Math.round((s.getWon() * 100.0) / s.getPlayed()) : 0;
            double avg = s.getWon() > 0
                    ? (double) sumWeighted(s.distributionArray()) / s.getWon()
                    : 999.0;
            entries.add(new LeaderboardEntry(0, username, s.getPlayed(), s.getWon(), winRate,
                    s.getMaxStreak(), s.getCurrentStreak(),
                    avg >= 999 ? "N/A" : String.format("%.2f", avg)));
        }

        if ("streak".equalsIgnoreCase(sortBy)) {
            entries.sort((a, b) -> {
                int c = Integer.compare(b.maxStreak(), a.maxStreak());
                return c != 0 ? c : Integer.compare(b.currentStreak(), a.currentStreak());
            });
        } else if ("avgGuesses".equalsIgnoreCase(sortBy)) {
            entries.sort((a, b) -> {
                double av = avgOf(a), bv = avgOf(b);
                return Double.compare(av, bv);
            });
        } else {
            entries.sort((a, b) -> {
                int c = Integer.compare(b.winRate(), a.winRate());
                return c != 0 ? c : Integer.compare(b.played(), a.played());
            });
        }

        for (int i = 0; i < entries.size(); i++) {
            entries.set(i, new LeaderboardEntry(i + 1, entries.get(i).username(),
                    entries.get(i).played(), entries.get(i).won(), entries.get(i).winRate(),
                    entries.get(i).maxStreak(), entries.get(i).currentStreak(),
                    entries.get(i).avgGuesses()));
        }

        return entries.size() > 100 ? entries.subList(0, 100) : entries;
    }

    private double avgOf(LeaderboardEntry e) {
        return "N/A".equals(e.avgGuesses()) ? 999.0 : Double.parseDouble(e.avgGuesses());
    }
}