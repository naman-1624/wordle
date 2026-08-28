package com.wordle.wordle.repository;

import com.wordle.wordle.model.CustomWordPack;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomWordPackRepository extends JpaRepository<CustomWordPack, Long> {

    Optional<CustomWordPack> findByNameIgnoreCase(String name);

    List<CustomWordPack> findAllByOrderByNameAsc();
}