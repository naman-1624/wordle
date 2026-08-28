package com.wordle.wordle.repository;

import com.wordle.wordle.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    long countByRole(String role);
}