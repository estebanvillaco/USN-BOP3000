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

    private final RestClient kassalappClient;
    private final RestClient spoonacularClient;
    private final String spoonacularKey;

    public DebugController(
            @Value("${kassalapp.api.key}") String kassalappKey,
            @Value("${spoonacular.api.key}") String spoonacularKey) {
        this.kassalappClient = RestClient.builder()
                .baseUrl("https://kassal.app/api/v1")
                .defaultHeader("Authorization", "Bearer " + kassalappKey)
                .build();
        this.spoonacularClient = RestClient.builder()
                .baseUrl("https://api.spoonacular.com")
                .build();
        this.spoonacularKey = spoonacularKey;
    }

    @GetMapping("/kassalapp")
    public String testKassalapp(@RequestParam(defaultValue = "pasta") String q) {
        return kassalappClient.get()
                .uri("/products?search={q}&size=3", q)
                .retrieve()
                .body(String.class);
    }

    @GetMapping("/spoonacular")
    public String testSpoonacular(@RequestParam(defaultValue = "dinner") String q) {
        return spoonacularClient.get()
                .uri("/recipes/complexSearch?apiKey={key}&query={q}&number=3", spoonacularKey, q)
                .retrieve()
                .body(String.class);
    }

    @GetMapping("/spoonacular-bulk")
    public String testSpoonacularBulk(@RequestParam(defaultValue = "715415,715446") String ids) {
        // Mirror exactly how SpoonacularService builds the URI (string concat, no template vars)
        String uri = "/recipes/informationBulk?apiKey=" + spoonacularKey + "&ids=" + ids;
        return spoonacularClient.get()
                .uri(uri)
                .retrieve()
                .body(String.class);
    }
}
