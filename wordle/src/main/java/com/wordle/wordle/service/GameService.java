package com.wordle.wordle.service;

import com.wordle.wordle.dto.GameHistoryView;
import com.wordle.wordle.dto.GameView;
import com.wordle.wordle.dto.GuessRequest;
import com.wordle.wordle.dto.GuessResult;
import com.wordle.wordle.dto.GuessView;
import com.wordle.wordle.dto.StartGameRequest;
import com.wordle.wordle.exception.GameException;
import com.wordle.wordle.model.CustomWordPack;
import com.wordle.wordle.model.GameRecord;
import com.wordle.wordle.model.GameStatus;
import com.wordle.wordle.model.User;
import com.wordle.wordle.repository.CustomWordPackRepository;
import com.wordle.wordle.repository.GameRecordRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class GameService {

    public static final int MAX_ATTEMPTS = 6;

    private final GameRecordRepository gameRepository;
    private final WordleService wordleService;
    private final PackService packService;
    private final StatsService statsService;
    private final CustomWordPackRepository packRepository;
    private final Random random = new Random();

    public GameService(GameRecordRepository gameRepository,
                       WordleService wordleService,
                       PackService packService,
                       StatsService statsService,
                       CustomWordPackRepository packRepository) {
        this.gameRepository = gameRepository;
        this.wordleService = wordleService;
        this.packService = packService;
        this.statsService = statsService;
        this.packRepository = packRepository;
    }

    @Transactional
    public GameView startGame(User user, StartGameRequest request) {
        GameRecord active = currentRecord(user.getId());
        if (active != null) {
            return toView(active);
        }

        String mode = request != null && request.mode() != null
                ? request.mode().trim().toLowerCase() : "normal";
        LocalDate today = LocalDate.now();

        GameRecord record = new GameRecord();
        record.setUserId(user.getId());
        record.setGameDate(today);

        if ("pack".equals(mode)) {
            if (request.packId() == null) {
                throw new GameException(HttpStatus.BAD_REQUEST, "Pack id is required for pack mode");
            }
            CustomWordPack pack = packService.getById(request.packId());
            List<String> words = packService.parseWords(pack);
            if (words.isEmpty()) {
                throw new GameException(HttpStatus.BAD_REQUEST, "Selected pack has no words");
            }
            record.setMode("pack");
            record.setPackId(pack.getId());
            record.setWord(words.get(random.nextInt(words.size())));
        } else {
            if (!"time".equals(mode) && !"sudden".equals(mode)) {
                mode = "normal";
            }
            if (gameRepository.existsByUserIdAndModeAndGameDateAndStatusIn(
                    user.getId(), mode, today, List.of(GameStatus.WON, GameStatus.LOST))) {
                throw new GameException(HttpStatus.CONFLICT, "You already played " + mode + " mode today");
            }
            record.setMode(mode);
            record.setPackId(null);
            record.setWord(wordleService.getWordForStandardMode(mode));
        }

        record.setStatus(GameStatus.IN_PROGRESS);
        record.setHistory("");
        gameRepository.save(record);
        return toView(record);
    }

    public GameView currentGame(Long userId) {
        GameRecord record = currentRecord(userId);
        return record == null ? null : toView(record);
    }

    public Map<String, Boolean> todayStatus(Long userId) {
        LocalDate today = LocalDate.now();
        Map<String, Boolean> status = new HashMap<>();
        for (String mode : List.of("normal", "time", "sudden")) {
            status.put(mode, gameRepository.existsByUserIdAndModeAndGameDateAndStatusIn(
                    userId, mode, today, List.of(GameStatus.WON, GameStatus.LOST)));
        }
        return status;
    }

    private GameRecord currentRecord(Long userId) {
        return gameRepository.findFirstByUserIdAndStatusOrderByPlayedAtDesc(userId, GameStatus.IN_PROGRESS)
                .orElse(null);
    }

    @Transactional
    public GuessResult guess(User user, Long gameId, GuessRequest request) {
        GameRecord record = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameException(HttpStatus.NOT_FOUND, "Game not found"));
        if (!record.getUserId().equals(user.getId())) {
            throw new GameException(HttpStatus.FORBIDDEN, "Not your game");
        }
        if (record.getStatus() != GameStatus.IN_PROGRESS) {
            throw new GameException(HttpStatus.BAD_REQUEST, "Game already finished");
        }
        if (request == null || request.guess() == null) {
            throw new GameException(HttpStatus.BAD_REQUEST, "Guess is required");
        }

        String guess = request.guess().trim().toUpperCase();
        if (guess.length() != 5 || !wordleService.isValidWord(guess)) {
            throw new GameException(HttpStatus.BAD_REQUEST, "Word not in list");
        }

        String result = wordleService.evaluate(guess, record.getWord());
        appendHistory(record, guess, result);
        record.setAttemptsUsed(record.getAttemptsUsed() + 1);
        if (request.durationSeconds() != null) {
            record.setDurationSeconds(Math.max(record.getDurationSeconds(), request.durationSeconds()));
        }

        if (result.equals("GGGGG")) {
            record.setStatus(GameStatus.WON);
            record.setWon(true);
        } else if ("sudden".equals(record.getMode())) {
            record.setStatus(GameStatus.LOST);
        } else if (record.getAttemptsUsed() >= MAX_ATTEMPTS) {
            record.setStatus(GameStatus.LOST);
        }

        gameRepository.save(record);

        if (record.getStatus() != GameStatus.IN_PROGRESS) {
            statsService.recordResult(record, user);
        }

        return new GuessResult(record.getId(), guess, result, record.getStatus().name(),
                record.getAttemptsUsed(), MAX_ATTEMPTS - record.getAttemptsUsed(), record.isWon());
    }

    @Transactional
    public GameView forfeit(User user, Long gameId, int durationSeconds) {
        GameRecord record = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameException(HttpStatus.NOT_FOUND, "Game not found"));
        if (!record.getUserId().equals(user.getId())) {
            throw new GameException(HttpStatus.FORBIDDEN, "Not your game");
        }
        if (record.getStatus() == GameStatus.IN_PROGRESS) {
            record.setStatus(GameStatus.LOST);
            if (durationSeconds > 0) record.setDurationSeconds(durationSeconds);
            gameRepository.save(record);
            statsService.recordResult(record, user);
        }
        return toView(record);
    }

    @Transactional
    public List<GameHistoryView> recentGames(Long userId, int limit, int offset) {
        List<GameRecord> records = gameRepository.findByUserIdOrderByPlayedAtDesc(
                userId, PageRequest.of(offset / Math.max(limit, 1), Math.max(limit, 1)));
        List<GameHistoryView> views = new ArrayList<>();
        for (GameRecord r : records) {
            String packName = null;
            if (r.getPackId() != null) {
                packName = packRepository.findById(r.getPackId())
                        .map(CustomWordPack::getName).orElse(null);
            }
            views.add(new GameHistoryView(r.getId(), r.getMode(), packName, r.getWord(),
                    r.getAttemptsUsed(), r.isWon(), r.getDurationSeconds(),
                    r.getPlayedAt().toString()));
        }
        return views;
    }

    private void appendHistory(GameRecord record, String guess, String result) {
        String token = guess + "|" + result;
        record.setHistory(record.getHistory() == null || record.getHistory().isBlank()
                ? token : record.getHistory() + "\n" + token);
    }

    private GameView toView(GameRecord record) {
        List<GuessView> history = new ArrayList<>();
        if (record.getHistory() != null && !record.getHistory().isBlank()) {
            for (String line : record.getHistory().split("\n")) {
                String[] parts = line.split("\\|");
                if (parts.length == 2) {
                    history.add(new GuessView(parts[0], parts[1]));
                }
            }
        }
        return new GameView(record.getId(), record.getMode(), record.getPackId(),
                record.getStatus().name(), record.getAttemptsUsed(),
                MAX_ATTEMPTS - record.getAttemptsUsed(), record.getDurationSeconds(), history);
    }
}