package com.wordle.wordle.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private final int dailyLimit;
    private final ConcurrentHashMap<String, Counter> counters = new ConcurrentHashMap<>();

    public RateLimitService(@Value("${wordle.reveal-daily-limit}") int dailyLimit) {
        this.dailyLimit = dailyLimit;
    }

    public boolean allow(String key) {
        LocalDate today = LocalDate.now();
        Counter counter = counters.computeIfAbsent(key, k -> new Counter(today, 0));
        if (!today.equals(counter.date)) {
            counter.date = today;
            counter.count = 0;
        }
        if (counter.count >= dailyLimit) {
            return false;
        }
        counter.count++;
        return true;
    }

    private static class Counter {
        LocalDate date;
        int count;

        Counter(LocalDate date, int count) {
            this.date = date;
            this.count = count;
        }
    }
}