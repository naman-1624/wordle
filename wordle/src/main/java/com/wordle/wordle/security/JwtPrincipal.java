package com.wordle.wordle.security;

import java.io.Serializable;

public record JwtPrincipal(Long userId, String username, String role) implements Serializable {}