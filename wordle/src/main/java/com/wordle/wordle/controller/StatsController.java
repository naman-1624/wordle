package com.wordle.wordle.controller;

import com.wordle.wordle.dto.LeaderboardEntry;
import com.wordle.wordle.dto.StatsView;
import com.wordle.wordle.security.CurrentUser;
import com.wordle.wordle.service.StatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/me")
    public ResponseEntity<StatsView> me() {
        return ResponseEntity.ok(statsService.viewFor(CurrentUser.get().userId()));
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<LeaderboardEntry>> leaderboard(
            @RequestParam(defaultValue = "winRate") String sortBy) {
        return ResponseEntity.ok(statsService.leaderboard(sortBy));
    }
}