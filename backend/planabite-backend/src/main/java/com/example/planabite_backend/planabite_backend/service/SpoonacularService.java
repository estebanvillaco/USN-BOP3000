package com.example.planabite_backend.planabite_backend.service;

import com.example.planabite_backend.planabite_backend.model.SpoonacularRecipe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
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
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromPath("/recipes/complexSearch")
                    .queryParam("apiKey", apiKey)
                    .queryParam("number", count);

            if (query != null && !query.isBlank()) {
                uriBuilder.queryParam("query", query);
            }
            if (diet != null && !diet.isBlank()) {
                uriBuilder.queryParam("diet", diet);
            }
            if (intolerances != null && !intolerances.isBlank()) {
                uriBuilder.queryParam("intolerances", intolerances);
            }

            String searchUri = uriBuilder.toUriString();
            log.info("Calling Spoonacular search: {}", searchUri.replaceAll("apiKey=[^&]+", "apiKey=***"));

            String searchBody = restClient.get()
                    .uri(searchUri)
                    .retrieve()
                    .body(String.class);

            if (searchBody == null) {
                log.warn("Null response from Spoonacular search");
                return List.of();
            }

            JsonNode searchRoot = objectMapper.readTree(searchBody);
            JsonNode results = searchRoot.path("results");

            if (!results.isArray() || results.isEmpty()) {
                log.warn("No recipes found from Spoonacular for query='{}', diet='{}'", query, diet);
                return List.of();
            }

            // Collect IDs and titles
            List<Long> ids = new ArrayList<>();
            List<String> titles = new ArrayList<>();
            for (JsonNode result : results) {
                long id = result.path("id").asLong(-1);
                String title = result.path("title").asText("");
                if (id > 0 && !title.isBlank()) {
                    ids.add(id);
                    titles.add(title);
                }
            }

            if (ids.isEmpty()) return List.of();

            // Step 2: bulk fetch full recipe info to get extendedIngredients
            String idsCsv = ids.stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(","));
            String bulkUri = "/recipes/informationBulk?apiKey=" + apiKey + "&ids=" + idsCsv;
            log.info("Fetching Spoonacular bulk info for {} recipes", ids.size());

            String bulkBody = restClient.get()
                    .uri(bulkUri)
                    .retrieve()
                    .body(String.class);

            if (bulkBody == null) {
                log.warn("Null response from Spoonacular bulk info");
                return List.of();
            }

            JsonNode bulkResults = objectMapper.readTree(bulkBody);
            if (!bulkResults.isArray()) {
                log.warn("Unexpected bulk info response structure");
                return List.of();
            }

            List<SpoonacularRecipe> recipes = new ArrayList<>();
            for (JsonNode recipe : bulkResults) {
                String title = recipe.path("title").asText("");
                if (title.isBlank()) continue;

                List<String> ingredients = new ArrayList<>();
                JsonNode extendedIngredients = recipe.path("extendedIngredients");
                if (extendedIngredients.isArray()) {
                    for (JsonNode ing : extendedIngredients) {
                        String name = ing.path("name").asText("");
                        if (!name.isBlank() && !ingredients.contains(name)) {
                            ingredients.add(name);
                        }
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
            return List.of();
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
