package com.wordle.wordle.controller;

import com.wordle.wordle.dto.PackView;
import com.wordle.wordle.service.PackService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/packs")
public class PackController {

    private final PackService packService;

    public PackController(PackService packService) {
        this.packService = packService;
    }

    @GetMapping
    public ResponseEntity<List<PackView>> list() {
        return ResponseEntity.ok(packService.listAll());
    }
}