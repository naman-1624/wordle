package com.wordle.wordle.controller;

import com.wordle.wordle.dto.GameView;
import com.wordle.wordle.dto.GuessRequest;
import com.wordle.wordle.dto.GuessResult;
import com.wordle.wordle.dto.StartGameRequest;
import com.wordle.wordle.dto.TodayStatusView;
import com.wordle.wordle.model.User;
import com.wordle.wordle.security.CurrentUser;
import com.wordle.wordle.service.AuthService;
import com.wordle.wordle.service.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService gameService;
    private final AuthService authService;

    public GameController(GameService gameService, AuthService authService) {
        this.gameService = gameService;
        this.authService = authService;
    }

    @PostMapping("/start")
    public ResponseEntity<?> start(@RequestBody(required = false) StartGameRequest request) {
        User user = authService.getById(CurrentUser.get().userId());
        return ResponseEntity.ok(gameService.startGame(user, request));
    }

    @GetMapping("/current")
    public ResponseEntity<?> current() {
        return ResponseEntity.ok(gameService.currentGame(CurrentUser.get().userId()));
    }

    @GetMapping("/status")
    public ResponseEntity<TodayStatusView> status() {
        Long userId = CurrentUser.get().userId();
        return ResponseEntity.ok(new TodayStatusView(
                gameService.todayStatus(userId),
                gameService.currentGame(userId)));
    }

    @PostMapping("/{gameId}/guess")
    public ResponseEntity<GuessResult> guess(@PathVariable Long gameId,
                                             @RequestBody(required = false) GuessRequest request) {
        User user = authService.getById(CurrentUser.get().userId());
        return ResponseEntity.ok(gameService.guess(user, gameId, request));
    }

    @PostMapping("/{gameId}/forfeit")
    public ResponseEntity<GameView> forfeit(@PathVariable Long gameId,
                                            @RequestBody(required = false) Map<String, Object> body) {
        User user = authService.getById(CurrentUser.get().userId());
        int duration = 0;
        if (body != null && body.get("durationSeconds") instanceof Number n) {
            duration = n.intValue();
        }
        return ResponseEntity.ok(gameService.forfeit(user, gameId, duration));
    }

    @GetMapping("/recent")
    public ResponseEntity<?> recent(@RequestParam(defaultValue = "20") int limit,
                                    @RequestParam(defaultValue = "0") int offset) {
        return ResponseEntity.ok(gameService.recentGames(CurrentUser.get().userId(), limit, offset));
    }
}