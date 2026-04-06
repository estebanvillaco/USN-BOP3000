package com.example.planabite_backend.planabite_backend.service;

<<<<<<< HEAD
import com.example.planabite_backend.planabite_backend.model.Meal;
=======
import com.example.planabite_backend.planabite_backend.model.IngredientItem;
>>>>>>> f19a9760d6658d1b444331042040346fb576e4b2
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class KassalappService {

    private static final Logger log = LoggerFactory.getLogger(KassalappService.class);

    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public KassalappService(@Value("${kassalapp.api.key}") String apiKey) {
        this.restClient = RestClient.builder()
                .baseUrl("https://kassal.app/api/v1")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

<<<<<<< HEAD
    // Products below this price are likely too small to represent a real meal ingredient
    private static final double MIN_PRODUCT_PRICE = 20.0;

    public Meal searchProduct(String query, int mealId, String day) {
        try {
            log.info("Searching Kassalapp for: '{}'", query);

            String responseBody = restClient.get()
                    .uri("/products?search={q}&size=10", query)
=======
    public IngredientItem searchIngredient(String query) {
        try {
            log.info("Searching Kassalapp for ingredient: '{}'", query);

            String responseBody = restClient.get()
                    .uri("/products?search={q}&size=5", query)
>>>>>>> f19a9760d6658d1b444331042040346fb576e4b2
                    .retrieve()
                    .body(String.class);

            if (responseBody == null) {
<<<<<<< HEAD
                log.warn("Null response from Kassalapp for query '{}'", query);
                return null;
            }

            log.info("Kassalapp raw response: {}", responseBody);

=======
                log.warn("Null response from Kassalapp for '{}'", query);
                return null;
            }

>>>>>>> f19a9760d6658d1b444331042040346fb576e4b2
            JsonNode response = objectMapper.readTree(responseBody);
            JsonNode data = response.get("data");

            if (data == null || !data.isArray() || data.isEmpty()) {
<<<<<<< HEAD
                log.warn("No products found for query '{}'", query);
                return null;
            }

            // Prefer the cheapest product at or above MIN_PRODUCT_PRICE.
            // Fall back to the overall cheapest if nothing meets the threshold.
            JsonNode bestAboveMin = null;
            double bestPriceAboveMin = Double.MAX_VALUE;
            JsonNode cheapestOverall = null;
            double cheapestPrice = Double.MAX_VALUE;

            for (JsonNode product : data) {
                double price = product.path("current_price").asDouble(0.0);
                if (price <= 0) continue;
                if (price < cheapestPrice) {
                    cheapestPrice = price;
                    cheapestOverall = product;
                }
                if (price >= MIN_PRODUCT_PRICE && price < bestPriceAboveMin) {
                    bestPriceAboveMin = price;
                    bestAboveMin = product;
                }
            }

            JsonNode selected = bestAboveMin != null ? bestAboveMin : cheapestOverall;
            double selectedPrice = bestAboveMin != null ? bestPriceAboveMin : cheapestPrice;

            if (selected == null) {
                log.warn("All products had zero/missing price for query '{}'", query);
                return null;
            }

            String name = selected.path("name").asText("Ukjent produkt");
            String storeName = selected.path("store").path("name").asText("Ukjent butikk");

            log.info("Selected: '{}' at {} kr from {}", name, selectedPrice, storeName);
            return new Meal(mealId, day, name, storeName, selectedPrice);
=======
                log.warn("No products found in Kassalapp for '{}'", query);
                return null;
            }

            JsonNode bestProduct = null;
            double bestPrice = Double.MAX_VALUE;

            for (JsonNode product : data) {
                double price = product.path("current_price").asDouble(0.0);
                if (price > 0 && price < bestPrice) {
                    bestPrice = price;
                    bestProduct = product;
                }
            }

            if (bestProduct == null) {
                log.warn("All products had zero/missing price for '{}'", query);
                return null;
            }

            String name = bestProduct.path("name").asText("Ukjent produkt");
            String storeName = bestProduct.path("store").path("name").asText("Ukjent butikk");

            log.info("Found: '{}' at {} kr from {}", name, bestPrice, storeName);
            return new IngredientItem(name, bestPrice, storeName);
>>>>>>> f19a9760d6658d1b444331042040346fb576e4b2

        } catch (Exception e) {
            log.error("Error searching Kassalapp for '{}': {}", query, e.getMessage(), e);
            return null;
        }
    }
}
