package com.example.webclient.controller;

import com.example.webclient.service.ApiService;
import com.example.webclient.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import reactor.core.publisher.Mono;

@Controller
public class WebController {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private ApiService apiService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("result", "");
        return "index";
    }

    @PostMapping("/call-without-token")
    public Mono<String> callWithoutToken(Model model) {
        return apiService.callApiWithoutToken()
                .map(result -> {
                    model.addAttribute("result", "Result (No Token): " + result);
                    model.addAttribute("lastAction", "Called API without token");
                    return "index";
                });
    }

    @PostMapping("/call-with-token")
    public Mono<String> callWithToken(Model model) {
        return tokenService.acquireToken()
                .flatMap(token -> {
                    System.out.println("DEBUG: Full token length: " + token.length());
                    System.out.println("DEBUG: Full token: " + token);
                    model.addAttribute("token", "Token acquired: " + token);
                    return apiService.callApiWithToken(token);
                })
                .map(result -> {
                    model.addAttribute("result", "Result (With Token): " + result);
                    model.addAttribute("lastAction", "Called API with token");
                    return "index";
                })
                .onErrorResume(error -> {
                    model.addAttribute("result", "Error acquiring token: " + error.getMessage());
                    model.addAttribute("lastAction", "Failed to acquire token");
                    return Mono.just("index");
                });
    }
}
