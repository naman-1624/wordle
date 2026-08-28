package com.wordle.wordle.dto;

import java.util.List;

public record PackRequest(String name, String description, List<String> words) {}