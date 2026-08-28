package com.wordle.wordle.controller;

import com.wordle.wordle.exception.GameException;
import com.wordle.wordle.security.CurrentUser;
import com.wordle.wordle.service.RateLimitService;
import com.wordle.wordle.service.WordInfoService;
import com.wordle.wordle.service.WordleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class WordleController {

    private final WordleService service;
    private final WordInfoService wordInfoService;
    private final RateLimitService rateLimitService;

    public WordleController(WordleService service,
                            WordInfoService wordInfoService,
                            RateLimitService rateLimitService) {
        this.service = service;
        this.wordInfoService = wordInfoService;
        this.rateLimitService = rateLimitService;
    }

    @GetMapping("/hint")
    public ResponseEntity<Map<String, String>> getHint(
            @RequestParam(defaultValue = "normal") String mode) {
        return ResponseEntity.ok(Map.of("hint", service.getHintForMode(mode)));
    }

    @GetMapping("/validate-word")
    public ResponseEntity<Map<String, Boolean>> validateWord(@RequestParam String word) {
        return ResponseEntity.ok(Map.of("valid", service.isValidWord(word)));
    }

    @GetMapping("/reveal-word")
    public ResponseEntity<Map<String, String>> revealWord(
            @RequestParam(defaultValue = "normal") String mode) {
        Long userId = CurrentUser.get().userId();
        if (!rateLimitService.allow("reveal:" + userId)) {
            throw new GameException(HttpStatus.TOO_MANY_REQUESTS,
                    "Daily reveal limit reached. Try again tomorrow.");
        }

        String word = service.getWordForStandardMode(mode);
        Map<String, String> response = new HashMap<>();
        response.put("word", word);
        response.put("definition", wordInfoService.definition(word));
        response.put("trivia", wordInfoService.trivia(word));
        response.put("example", wordInfoService.example(word));
        return ResponseEntity.ok(response);
    }
}