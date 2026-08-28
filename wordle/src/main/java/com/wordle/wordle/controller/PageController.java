package com.wordle.wordle.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String home() {
        return "forward:/landing.html";
    }

    @GetMapping("/game")
    public String game() {
        return "forward:/index.html";
    }

    @GetMapping("/login")
    public String login() {
        return "forward:/login.html";
    }

    @GetMapping("/hints")
    public String hints() {
        return "forward:/hints.html";
    }

    @GetMapping("/statistics")
    public String statistics() {
        return "forward:/statistics.html";
    }

    @GetMapping("/loss")
    public String loss() {
        return "forward:/loss.html";
    }

    @GetMapping("/leaderboard")
    public String leaderboard() {
        return "forward:/leaderboard.html";
    }

    @GetMapping("/terms-of-sale")
    public String termsOfSale() {
        return "forward:/terms-of-sale.html";
    }

    @GetMapping("/terms-of-service")
    public String termsOfService() {
        return "forward:/terms-of-service.html";
    }

    @GetMapping("/privacy-policy")
    public String privacyPolicy() {
        return "forward:/privacy-policy.html";
    }

    @GetMapping("/help")
    public String help() {
        return "forward:/help.html";
    }

    @GetMapping("/contact")
    public String contact() {
        return "forward:/contact.html";
    }

    @GetMapping("/admin")
    public String admin() {
        return "forward:/admin.html";
    }
}