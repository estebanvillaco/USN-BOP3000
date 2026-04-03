package com.example.planabite_backend.planabite_backend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    private final RestClient restClient;

    public DebugController(@Value("${kassalapp.api.key}") String apiKey) {
        this.restClient = RestClient.builder()
                .baseUrl("https://kassal.app/api/v1")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    @GetMapping("/kassalapp")
    public String testKassalapp(@RequestParam(defaultValue = "pasta") String q) {
        return restClient.get()
                .uri("/products?search={q}&size=3", q)
                .retrieve()
                .body(String.class);
    }
}
