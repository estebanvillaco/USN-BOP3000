package com.example.planabite_backend.planabite_backend.service;

import com.example.planabite_backend.planabite_backend.model.SpoonacularIngredient;
import com.example.planabite_backend.planabite_backend.model.SpoonacularRecipe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Service
public class SpoonacularService {

    private static final Logger log = LoggerFactory.getLogger(SpoonacularService.class);

    private final RestClient restClient;
    private final String apiKey;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SpoonacularService(@Value("${spoonacular.api.key}") String apiKey) {
        this.apiKey = apiKey;
        this.restClient = RestClient.builder()
                .baseUrl("https://api.spoonacular.com")
                .build();
    }

    public List<SpoonacularRecipe> findRecipes(String query, String diet, String intolerances, int count) {
        try {
            // Step 1: search for recipe IDs and titles
            // Build URI manually and wrap in java.net.URI so RestClient does not re-encode it
            StringBuilder searchUrl = new StringBuilder("https://api.spoonacular.com/recipes/complexSearch")
                    .append("?apiKey=").append(apiKey)
                    .append("&number=").append(count);
            if (query != null && !query.isBlank()) {
                searchUrl.append("&query=").append(java.net.URLEncoder.encode(query, java.nio.charset.StandardCharsets.UTF_8));
            }
            if (diet != null && !diet.isBlank()) {
                searchUrl.append("&diet=").append(java.net.URLEncoder.encode(diet, java.nio.charset.StandardCharsets.UTF_8));
            }
            if (intolerances != null && !intolerances.isBlank()) {
                searchUrl.append("&intolerances=").append(java.net.URLEncoder.encode(intolerances, java.nio.charset.StandardCharsets.UTF_8));
            }

            java.net.URI searchUri = java.net.URI.create(searchUrl.toString());
            log.info("Calling Spoonacular search: {}", searchUrl.toString().replaceAll("apiKey=[^&]+", "apiKey=***"));

            String searchBody = restClient.get()
                    .uri(searchUri)
                    .retrieve()
                    .body(String.class);

            if (searchBody == null) {
                throw new IllegalStateException("Null response from Spoonacular search");
            }

            JsonNode searchRoot = objectMapper.readTree(searchBody);

            // Surface Spoonacular API errors (quota, auth, etc.)
            if (searchRoot.has("status") && "failure".equals(searchRoot.path("status").asText())) {
                String msg = searchRoot.path("message").asText("ukjent feil");
                int code = searchRoot.path("code").asInt(0);
                throw new IllegalStateException("Spoonacular API-feil (kode " + code + "): " + msg);
            }

            JsonNode results = searchRoot.path("results");

            if (!results.isArray() || results.isEmpty()) {
                throw new IllegalStateException("Ingen oppskrifter funnet. Rårespons: " + searchBody.substring(0, Math.min(300, searchBody.length())));
            }

            // Collect IDs and titles
            List<Long> ids = new ArrayList<>();
            List<String> titles = new ArrayList<>();
            for (JsonNode result : results) {
                JsonNode idNode = result.path("id");
                String title = result.path("title").asText("");
                log.info("Parsed search result: id={} (isNumber={}) title='{}'", idNode, idNode.isNumber(), title);
                if (idNode.isNumber() && !title.isBlank()) {
                    ids.add(idNode.asLong());
                    titles.add(title);
                }
            }

            if (ids.isEmpty()) {
                throw new IllegalStateException("Fikk søkeresultater fra Spoonacular, men klarte ikke å parse IDene. Rårespons: " + searchBody.substring(0, Math.min(300, searchBody.length())));
            }

            // Step 2: bulk fetch full recipe info to get extendedIngredients
            // Use java.net.URI to prevent Spring from re-encoding the comma in ids
            String idsCsv = ids.stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(","));
            java.net.URI bulkUri = java.net.URI.create(
                "https://api.spoonacular.com/recipes/informationBulk?apiKey=" + apiKey + "&ids=" + idsCsv
            );
            log.info("Fetching Spoonacular bulk info for {} recipes: {}", ids.size(), idsCsv);

            String bulkBody = restClient.get()
                    .uri(bulkUri)
                    .retrieve()
                    .body(String.class);

            if (bulkBody == null) {
                throw new IllegalStateException("Null response from Spoonacular bulk info");
            }

            JsonNode bulkResults = objectMapper.readTree(bulkBody);
            if (!bulkResults.isArray()) {
                throw new IllegalStateException("Uventet respons fra Spoonacular bulk-endepunkt: " + bulkBody.substring(0, Math.min(300, bulkBody.length())));
            }

            List<SpoonacularRecipe> recipes = new ArrayList<>();
            for (JsonNode recipe : bulkResults) {
                String title = recipe.path("title").asText("");
                if (title.isBlank()) continue;

                List<SpoonacularIngredient> ingredients = new ArrayList<>();
                JsonNode extendedIngredients = recipe.path("extendedIngredients");
                if (extendedIngredients.isArray()) {
                    for (JsonNode ing : extendedIngredients) {
                        String name = ing.path("name").asText("");
                        if (name.isBlank()) continue;
                        boolean alreadyAdded = ingredients.stream().anyMatch(i -> i.name().equals(name));
                        if (alreadyAdded) continue;

                        String metricAmt  = ing.path("measures").path("metric").path("amount").asText("");
                        String metricUnit = ing.path("measures").path("metric").path("unitShort").asText("");
                        String amount = metricAmt.isBlank() ? "1 stk" : (metricAmt + " " + metricUnit).trim();

                        ingredients.add(new SpoonacularIngredient(name, amount));
                    }
                }

                if (!ingredients.isEmpty()) {
                    recipes.add(new SpoonacularRecipe(title, ingredients));
                    log.info("Recipe: '{}' with {} ingredients", title, ingredients.size());
                } else {
                    log.warn("Recipe '{}' had no extendedIngredients in bulk response", title);
                }
            }

            return recipes;

        } catch (Exception e) {
            log.error("Error calling Spoonacular: {}", e.getMessage(), e);
            throw new RuntimeException("Spoonacular-feil: " + e.getMessage(), e);
        }
    }

    public String mapDietType(String dietType) {
        if (dietType == null) return "";
        return switch (dietType) {
            case "Vegetarian" -> "vegetarian";
            case "Vegan" -> "vegan";
            case "Lactose Free" -> "dairy free";
            case "Gluten Free" -> "gluten free";
            default -> "";
        };
    }

    public String mapIntolerances(String allergies) {
        if (allergies == null || allergies.isBlank()) return "";

        List<String> result = new ArrayList<>();
        for (String term : allergies.split(",")) {
            String t = term.trim().toLowerCase();
            if (t.contains("nøtt") || t.contains("peanøtt") || t.contains("noett")) {
                if (!result.contains("peanut")) result.add("peanut");
                if (!result.contains("tree nut")) result.add("tree nut");
            }
            if (t.contains("melk") || t.contains("laktose") || t.contains("dairy")) {
                if (!result.contains("dairy")) result.add("dairy");
            }
            if (t.contains("hvete") || t.contains("gluten") || t.contains("wheat")) {
                if (!result.contains("gluten")) result.add("gluten");
                if (!result.contains("wheat")) result.add("wheat");
            }
            if (t.contains("egg")) {
                if (!result.contains("egg")) result.add("egg");
            }
            if (t.contains("fisk") || t.contains("seafood")) {
                if (!result.contains("seafood")) result.add("seafood");
            }
            if (t.contains("skalldyr") || t.contains("shellfish")) {
                if (!result.contains("shellfish")) result.add("shellfish");
            }
            if (t.contains("soya") || t.contains("soy")) {
                if (!result.contains("soy")) result.add("soy");
            }
        }
        return String.join(",", result);
    }
}
