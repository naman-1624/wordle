package com.wordle.wordle.service;

import com.wordle.wordle.dto.PackView;
import com.wordle.wordle.exception.GameException;
import com.wordle.wordle.model.CustomWordPack;
import com.wordle.wordle.repository.CustomWordPackRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PackService {

    private static final int MIN_WORDS = 10;

    private final CustomWordPackRepository packRepository;
    private final WordleService wordleService;

    public PackService(CustomWordPackRepository packRepository, WordleService wordleService) {
        this.packRepository = packRepository;
        this.wordleService = wordleService;
    }

    public List<PackView> listAll() {
        return packRepository.findAllByOrderByNameAsc().stream()
                .map(p -> new PackView(p.getId(), p.getName(), p.getDescription(),
                        countWords(p.getWords())))
                .collect(Collectors.toList());
    }

    public CustomWordPack getById(Long id) {
        return packRepository.findById(id)
                .orElseThrow(() -> new GameException(HttpStatus.NOT_FOUND, "Pack not found"));
    }

    @Transactional
    public CustomWordPack create(String name, String description, List<String> words, Long adminId) {
        validateNew(name, words);
        CustomWordPack pack = new CustomWordPack();
        pack.setName(name);
        pack.setDescription(description);
        pack.setWords(join(words));
        pack.setCreatedBy(adminId);
        return packRepository.save(pack);
    }

    @Transactional
    public CustomWordPack update(Long id, String name, String description, List<String> words) {
        CustomWordPack pack = getById(id);
        if (words != null && !words.isEmpty()) validateWords(words);
        if (name != null && !name.isBlank()) {
            packRepository.findByNameIgnoreCase(name)
                    .filter(existing -> !existing.getId().equals(id))
                    .ifPresent(existing -> { throw new GameException(HttpStatus.CONFLICT, "Pack name already exists"); });
            pack.setName(name);
        }
        if (description != null) pack.setDescription(description);
        if (words != null && !words.isEmpty()) pack.setWords(join(words));
        return packRepository.save(pack);
    }

    @Transactional
    public void delete(Long id) {
        CustomWordPack pack = getById(id);
        packRepository.delete(pack);
    }

    private void validateNew(String name, List<String> words) {
        if (name == null || name.isBlank() || name.length() > 50) {
            throw new GameException(HttpStatus.BAD_REQUEST, "Pack name is required (max 50 chars)");
        }
        if (packRepository.findByNameIgnoreCase(name.trim()).isPresent()) {
            throw new GameException(HttpStatus.CONFLICT, "Pack name already exists");
        }
        validateWords(words);
    }

    private void validateWords(List<String> words) {
        if (words == null || words.isEmpty()) {
            throw new GameException(HttpStatus.BAD_REQUEST, "Pack must contain words");
        }
        Set<String> unique = new LinkedHashSet<>();
        for (String raw : words) {
            if (raw == null) continue;
            String w = raw.trim().toUpperCase();
            if (!wordleService.isValidWord(w)) {
                throw new GameException(HttpStatus.BAD_REQUEST, "Not a valid word: " + w);
            }
            unique.add(w);
        }
        if (unique.size() < MIN_WORDS) {
            throw new GameException(HttpStatus.BAD_REQUEST, "Pack needs at least " + MIN_WORDS + " valid 5-letter words");
        }
    }

    private String join(List<String> words) {
        Set<String> unique = new LinkedHashSet<>();
        for (String raw : words) {
            if (raw != null) unique.add(raw.trim().toUpperCase());
        }
        return String.join(",", unique);
    }

    private int countWords(String stored) {
        if (stored == null || stored.isBlank()) return 0;
        return stored.split(",").length;
    }

    public List<String> parseWords(CustomWordPack pack) {
        if (pack.getWords() == null || pack.getWords().isBlank()) return List.of();
        return Arrays.stream(pack.getWords().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}