package com.wordle.wordle.repository;

import com.wordle.wordle.model.GameRecord;
import com.wordle.wordle.model.GameStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface GameRecordRepository extends JpaRepository<GameRecord, Long> {

    Optional<GameRecord> findFirstByUserIdAndStatusOrderByPlayedAtDesc(Long userId, GameStatus status);

    boolean existsByUserIdAndModeAndGameDateAndStatusIn(
            Long userId, String mode, LocalDate gameDate, List<GameStatus> statuses);

    List<GameRecord> findByUserIdOrderByPlayedAtDesc(Long userId, Pageable pageable);

    List<GameRecord> findByUserIdAndStatusOrderByPlayedAtDesc(Long userId, GameStatus status);

    List<GameRecord> findByStatusAndPlayedAtAfter(GameStatus status, Instant after);

    List<GameRecord> findByPlayedAtAfter(Instant after);

    long countByPlayedAtAfter(Instant after);

    long countByStatus(GameStatus status);
}