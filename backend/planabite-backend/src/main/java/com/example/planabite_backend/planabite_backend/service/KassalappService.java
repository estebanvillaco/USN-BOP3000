package com.example.planabite_backend.planabite_backend.service;

import com.example.planabite_backend.planabite_backend.model.Meal;
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

    public Meal searchProduct(String query, int mealId, String day, double minPrice, String preferredStore) {
        try {
            log.info("Searching Kassalapp for: '{}'", query);

            boolean filterByStore = preferredStore != null && !preferredStore.isBlank();

            JsonNode data = fetchProducts(query, filterByStore);
            if (data == null) return null;

            // Collect candidates matching store filter and exclusion rules
            java.util.List<JsonNode> candidates = new java.util.ArrayList<>();
            JsonNode noPriceStoreProduct = null;

            for (JsonNode product : data) {
                if (isExcludedProduct(product)) continue;
                if (filterByStore) {
                    String productStore = product.path("store").path("name").asText("").toLowerCase();
                    String preferred = preferredStore.toLowerCase();
                    if (!productStore.contains(preferred) && !preferred.contains(productStore)) continue;
                }
                double price = product.path("current_price").asDouble(0.0);
                if (price <= 0) {
                    if (noPriceStoreProduct == null) noPriceStoreProduct = product;
                    continue;
                }
                candidates.add(product);
            }

            // Sort candidates: higher relevance first, then cheapest within same relevance tier
            candidates.sort((a, b) -> {
                int scoreA = relevanceScore(a.path("name").asText(""), query);
                int scoreB = relevanceScore(b.path("name").asText(""), query);
                if (scoreB != scoreA) return Integer.compare(scoreB, scoreA);
                double priceA = a.path("current_price").asDouble(Double.MAX_VALUE);
                double priceB = b.path("current_price").asDouble(Double.MAX_VALUE);
                return Double.compare(priceA, priceB);
            });

            // Pick best priced product at or above minPrice within the top relevance tier,
            // falling back to cheapest within that tier, then lower tiers
            JsonNode selected = null;
            double selectedPrice = 0.0;

            if (!candidates.isEmpty()) {
                int topScore = relevanceScore(candidates.get(0).path("name").asText(""), query);
                JsonNode bestAboveMin = null;
                double bestPriceAboveMin = Double.MAX_VALUE;
                JsonNode cheapestInTier = null;
                double cheapestInTierPrice = Double.MAX_VALUE;

                for (JsonNode product : candidates) {
                    int score = relevanceScore(product.path("name").asText(""), query);
                    // Once we leave the top relevance tier and already have a priced candidate, stop
                    if (score < topScore && cheapestInTier != null) break;
                    double price = product.path("current_price").asDouble(0.0);
                    if (price < cheapestInTierPrice) {
                        cheapestInTierPrice = price;
                        cheapestInTier = product;
                    }
                    if (price >= minPrice && price < bestPriceAboveMin) {
                        bestPriceAboveMin = price;
                        bestAboveMin = product;
                    }
                }

                selected = bestAboveMin != null ? bestAboveMin : cheapestInTier;
                selectedPrice = bestAboveMin != null ? bestPriceAboveMin : cheapestInTierPrice;
            }

            if (selected == null) selected = noPriceStoreProduct;

            if (selected == null) {
                log.warn("No product found for query '{}' at store '{}'. Available stores in results: {}",
                        query, preferredStore, getStoreNames(data));
                return null;
            }

            String name = selected.path("name").asText("Ukjent produkt");
            String storeName = selected.path("store").path("name").asText("Ukjent butikk");

            log.info("Selected: '{}' at {} kr from {}", name, selectedPrice, storeName);
            return new Meal(mealId, day, name, storeName, selectedPrice, 0);

        } catch (Exception e) {
            log.error("Error searching Kassalapp for '{}': {}", query, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Returns the best matching product per store for the given query.
     * Used by the "cheapest store" mode to compare store totals across ingredients.
     */
    public java.util.Map<String, Meal> searchProductAllStores(String query, int mealId, String day, double minPrice) {
        try {
            log.info("Searching all stores for: '{}'", query);
            JsonNode data = fetchProducts(query, true);
            if (data == null) return java.util.Map.of();

            java.util.Map<String, JsonNode> bestProductByStore = new java.util.LinkedHashMap<>();
            java.util.Map<String, Integer> bestScoreByStore   = new java.util.LinkedHashMap<>();
            java.util.Map<String, Double>  bestPriceByStore   = new java.util.LinkedHashMap<>();

            for (JsonNode product : data) {
                if (isExcludedProduct(product)) continue;
                double price = product.path("current_price").asDouble(0.0);
                if (price <= 0) continue;
                String storeName = product.path("store").path("name").asText("");
                if (storeName.isBlank()) continue;

                int score = relevanceScore(product.path("name").asText(""), query);
                int currentScore = bestScoreByStore.getOrDefault(storeName, -1);
                double currentPrice = bestPriceByStore.getOrDefault(storeName, Double.MAX_VALUE);

                boolean better;
                if (score != currentScore) {
                    better = score > currentScore;
                } else {
                    boolean newMeetsMin     = price >= minPrice;
                    boolean currentMeetsMin = currentPrice >= minPrice;
                    if (newMeetsMin != currentMeetsMin) better = newMeetsMin;
                    else better = price < currentPrice;
                }

                if (better) {
                    bestProductByStore.put(storeName, product);
                    bestScoreByStore.put(storeName, score);
                    bestPriceByStore.put(storeName, price);
                }
            }

            java.util.Map<String, Meal> result = new java.util.LinkedHashMap<>();
            for (java.util.Map.Entry<String, JsonNode> entry : bestProductByStore.entrySet()) {
                String store = entry.getKey();
                String name  = entry.getValue().path("name").asText("Ukjent produkt");
                double price = bestPriceByStore.get(store);
                result.put(store, new Meal(mealId, day, name, store, price, 0));
            }
            log.info("Found products for '{}' at {} stores", query, result.size());
            return result;

        } catch (Exception e) {
            log.error("Error searching all stores for '{}': {}", query, e.getMessage(), e);
            return java.util.Map.of();
        }
    }

    private JsonNode fetchProducts(String query, boolean largeResultSet) {
        try {
            // Fetch up to 3 pages when store filtering is needed to ensure all stores are covered
            int pageSize = 100;
            int maxPages = largeResultSet ? 3 : 1;

            tools.jackson.databind.node.ArrayNode combined = objectMapper.createArrayNode();

            for (int page = 1; page <= maxPages; page++) {
                String responseBody = restClient.get()
                        .uri("/products?search={q}&size={size}&page={page}", query, pageSize, page)
                        .retrieve()
                        .body(String.class);

                if (responseBody == null) break;

                log.debug("Kassalapp page {} for '{}': fetched", page, query);

                JsonNode response = objectMapper.readTree(responseBody);
                JsonNode data = response.get("data");

                if (data == null || !data.isArray() || data.isEmpty()) break;

                data.forEach(combined::add);

                // Stop early if the API returned fewer results than requested (last page)
                if (data.size() < pageSize) break;
            }

            if (combined.isEmpty()) {
                log.warn("No products found for query '{}'", query);
                return null;
            }

            return combined;
        } catch (Exception e) {
            log.error("Error fetching products for '{}': {}", query, e.getMessage(), e);
            return null;
        }
    }

    // Common Norwegian noun inflection suffixes (plural, definite, genitive, etc.)
    private static final java.util.Set<String> INFLECTION_SUFFIXES = java.util.Set.of(
            "e", "en", "et", "er", "ene", "ens", "ets", "ers", "s"
    );

    /**
     * Scores how closely a product name matches the search query, handling Norwegian inflections.
     *
     * 4 = whole product name is query or inflected query (e.g. "poteter" for "potet")
     * 3 = first word of name is query or inflected form (e.g. "Poteter småpakke" for "potet")
     * 2 = any later word in name is query or inflected form (e.g. "Gul løk" for "løk")
     * 0 = query only appears inside a compound word (e.g. "løkchips" for "løk" → skip)
     *
     * This prevents "løkchips" beating "gul løk", and "fløtegratinerte poteter" beating "poteter".
     */
    private int relevanceScore(String productName, String query) {
        String name = productName.toLowerCase().trim();
        String q = query.toLowerCase().trim();

        // Score 4: entire product name is the query (exact or inflected)
        if (name.equals(q) || isInflectedForm(name, q)) return 4;

        String[] words = name.split("[\\s,/&]+");

        // Score 3: first word is query or inflected form
        if (words.length > 0 && (words[0].equals(q) || isInflectedForm(words[0], q))) return 3;

        // Score 2: any subsequent word is query or inflected form
        for (int i = 1; i < words.length; i++) {
            if (words[i].equals(q) || isInflectedForm(words[i], q)) return 2;
        }

        // Score 0: query only buried inside a compound word — not a relevant match
        return 0;
    }

    /** Returns true if {@code word} is an inflected form of {@code base} (e.g. "poteter" → "potet"). */
    private boolean isInflectedForm(String word, String base) {
        if (!word.startsWith(base) || word.equals(base)) return false;
        String suffix = word.substring(base.length());
        return INFLECTION_SUFFIXES.contains(suffix);
    }

    private java.util.Set<String> getStoreNames(JsonNode data) {
        java.util.Set<String> stores = new java.util.LinkedHashSet<>();
        for (JsonNode product : data) {
            String storeName = product.path("store").path("name").asText("");
            if (!storeName.isBlank()) stores.add(storeName);
        }
        return stores;
    }

    private boolean isExcludedProduct(JsonNode product) {
        String name = product.path("name").asText("").toLowerCase();

        // Baby food
        if (name.contains("baby") || name.contains("babymat") || name.contains("spedbarn") ||
            name.contains("barnemat") || name.contains("barnegrøt") || name.contains("småbarn") ||
            name.matches(".*\\b\\d+\\s*mnd\\b.*") || name.matches(".*\\bfra \\d+\\b.*")) {
            return true;
        }

        // Candy, lozenges, throat drops
        if (name.contains("fisherman") || name.contains("pastill") || name.contains("halspastill") ||
            name.contains("godteri") || name.contains("karamell") || name.contains("drops") ||
            name.contains("tyggegummi") || name.contains("lakrisbiter") || name.contains("sjokolade") ||
            name.contains("slikkeri")) {
            return true;
        }

        // Snacks and junk food
        if (name.contains("chips") || name.contains("popcorn") || name.contains("poppet") ||
            name.contains("snack") || name.contains("kjeks") || name.contains("kli-bolle") ||
            name.contains("potetgull") || name.contains("crispy") || name.contains("sprøstekt") ||
            name.contains("strøløk") || name.contains("peanøtt") || name.contains("nøttemiks")) {
            return true;
        }

        // Ready-made meals and powders
        if (name.contains("ferdigmat") || name.contains("ferdigrett") || name.contains("middagspose") ||
            name.contains("posematrett") || name.contains("protein pulver") || name.contains("proteinpulver") ||
            name.contains("kosttilskudd")) {
            return true;
        }

        return false;
    }
}
