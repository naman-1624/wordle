package com.wordle.wordle.controller;

import com.wordle.wordle.dto.AdminOverviewView;
import com.wordle.wordle.dto.AdminUserView;
import com.wordle.wordle.dto.PackRequest;
import com.wordle.wordle.dto.PackView;
import com.wordle.wordle.model.CustomWordPack;
import com.wordle.wordle.model.GameStatus;
import com.wordle.wordle.model.User;
import com.wordle.wordle.model.UserStats;
import com.wordle.wordle.repository.CustomWordPackRepository;
import com.wordle.wordle.repository.GameRecordRepository;
import com.wordle.wordle.repository.UserRepository;
import com.wordle.wordle.repository.UserStatsRepository;
import com.wordle.wordle.security.CurrentUser;
import com.wordle.wordle.service.PackService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final UserStatsRepository statsRepository;
    private final GameRecordRepository gameRepository;
    private final PackService packService;
    private final CustomWordPackRepository packRepository;

    public AdminController(UserRepository userRepository,
                           UserStatsRepository statsRepository,
                           GameRecordRepository gameRepository,
                           PackService packService,
                           CustomWordPackRepository packRepository) {
        this.userRepository = userRepository;
        this.statsRepository = statsRepository;
        this.gameRepository = gameRepository;
        this.packService = packService;
        this.packRepository = packRepository;
    }

    @GetMapping("/overview")
    public ResponseEntity<AdminOverviewView> overview() {
        Instant last24h = Instant.now().minusSeconds(86400);
        long gamesToday = gameRepository.countByPlayedAtAfter(last24h);
        long totalUsers = userRepository.count();
        long totalGames = gameRepository.count();
        long gamesWon = gameRepository.countByStatus(GameStatus.WON);

        int activePlayers = (int) statsRepository
                .findAll()
                .stream()
                .filter(s -> s.getPlayed() > 0)
                .count();

        return ResponseEntity.ok(new AdminOverviewView(totalUsers, totalGames,
                gamesToday, gamesWon, activePlayers));
    }

    @GetMapping("/users")
    public ResponseEntity<List<AdminUserView>> users() {
        List<AdminUserView> views = new ArrayList<>();
        for (User user : userRepository.findAll()) {
            UserStats stats = statsRepository.findByUserId(user.getId()).orElse(null);
            views.add(new AdminUserView(user.getId(), user.getUsername(), user.getRole(),
                    stats == null ? 0 : stats.getPlayed(),
                    stats == null ? 0 : stats.getWon(),
                    stats == null ? 0 : stats.getCurrentStreak(),
                    stats == null ? 0 : stats.getMaxStreak(),
                    user.getCreatedAt().toString()));
        }
        views.sort((a, b) -> Integer.compare(b.played(), a.played()));
        return ResponseEntity.ok(views);
    }

    @GetMapping("/packs")
    public ResponseEntity<List<PackView>> packs() {
        List<PackView> views = new ArrayList<>();
        for (CustomWordPack pack : packRepository.findAll()) {
            int count = pack.getWords() == null || pack.getWords().isBlank()
                    ? 0 : pack.getWords().split(",").length;
            views.add(new PackView(pack.getId(), pack.getName(), pack.getDescription(), count));
        }
        return ResponseEntity.ok(views);
    }

    @GetMapping("/packs/{id}")
    public ResponseEntity<?> packDetails(@PathVariable Long id) {
        CustomWordPack pack = packService.getById(id);
        return ResponseEntity.ok(java.util.Map.of(
                "id", pack.getId(),
                "name", pack.getName(),
                "description", pack.getDescription() == null ? "" : pack.getDescription(),
                "words", packService.parseWords(pack)));
    }

    @PostMapping("/packs")
    public ResponseEntity<PackView> createPack(@RequestBody(required = false) PackRequest request) {
        if (request == null) request = new PackRequest(null, null, List.of());
        Long adminId = CurrentUser.get().userId();
        CustomWordPack created = packService.create(
                request.name(), request.description(), request.words(), adminId);
        return ResponseEntity.ok(new PackView(created.getId(), created.getName(),
                created.getDescription(), created.getWords().split(",").length));
    }

    @PutMapping("/packs/{id}")
    public ResponseEntity<PackView> updatePack(@PathVariable Long id,
                                               @RequestBody(required = false) PackRequest request) {
        if (request == null) request = new PackRequest(null, null, List.of());
        CustomWordPack updated = packService.update(
                id, request.name(), request.description(), request.words());
        return ResponseEntity.ok(new PackView(updated.getId(), updated.getName(),
                updated.getDescription(), updated.getWords().split(",").length));
    }

    @DeleteMapping("/packs/{id}")
    public ResponseEntity<?> deletePack(@PathVariable Long id) {
        packService.delete(id);
        return ResponseEntity.ok(java.util.Map.of("deleted", true));
    }
}