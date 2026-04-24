package com.travelplanner.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplanner.dto.AiPlanResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class LlmPlannerService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${llm.gemini.api-key}")
    private String geminiApiKey;

        @Value("${llm.gemini.model:gemini-2.0-flash}")
    private String geminiModel;

        private static final List<String> GEMINI_MODEL_FALLBACKS = Arrays.asList(
            "gemini-2.0-flash",
            "gemini-2.0-flash-001",
            "gemini-flash-latest",
            "gemini-2.5-flash"
        );

    public LlmPlannerService(WebClient webClient) {
        this.webClient = webClient;
    }

    public AiPlanResponse generatePlan(String destination, Double budget, Integer duration, List<String> interests) {
        String normalizedDestination = destination == null || destination.isBlank() ? "Custom destination" : destination.trim();
        String prompt = buildPrompt(normalizedDestination, budget, duration, interests);

        String rawContent;

        try {
            rawContent = callGemini(prompt);
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode().value() == 429) {
                return buildFallbackPlan(normalizedDestination, budget, duration, interests,
                        "Gemini quota exceeded. Returned local fallback itinerary.");
            }
            throw new IllegalStateException("Gemini API error " + ex.getStatusCode().value() + ": " + ex.getResponseBodyAsString());
        } catch (Exception ex) {
            return buildFallbackPlan(normalizedDestination, budget, duration, interests,
                    "Gemini unavailable. Returned local fallback itinerary.");
        }

        return parseResponse(rawContent, normalizedDestination, budget, duration, interests);
    }

    private String callGemini(String prompt) {
        String effectiveApiKey = geminiApiKey == null ? "" : geminiApiKey.trim();
        String configuredModel = geminiModel == null ? "" : geminiModel.trim();
        if (effectiveApiKey.startsWith("\"") && effectiveApiKey.endsWith("\"") && effectiveApiKey.length() > 1) {
            effectiveApiKey = effectiveApiKey.substring(1, effectiveApiKey.length() - 1);
        }
        if (configuredModel.startsWith("\"") && configuredModel.endsWith("\"") && configuredModel.length() > 1) {
            configuredModel = configuredModel.substring(1, configuredModel.length() - 1);
        }
        if (configuredModel.startsWith("models/")) {
            configuredModel = configuredModel.substring("models/".length());
        }

        if (effectiveApiKey.isBlank()) {
            throw new IllegalStateException("GEMINI_API_KEY is missing");
        }
        if (configuredModel.isBlank()) {
            configuredModel = "gemini-2.0-flash";
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))));
        payload.put("generationConfig", Map.of("temperature", 0.3));

        List<String> modelCandidates = new ArrayList<>();
        modelCandidates.add(configuredModel);
        for (String candidate : GEMINI_MODEL_FALLBACKS) {
            if (!modelCandidates.contains(candidate)) {
                modelCandidates.add(candidate);
            }
        }

        JsonNode response = null;
        WebClientResponseException last404 = null;
        for (String model : modelCandidates) {
            try {
                response = generateContent(model, effectiveApiKey, payload);
                break;
            } catch (WebClientResponseException ex) {
                if (ex.getStatusCode().value() == 404) {
                    last404 = ex;
                    continue;
                }
                throw ex;
            }
        }

        if (response == null && last404 != null) {
            throw new IllegalStateException("No compatible Gemini model found. Tried: " + String.join(", ", modelCandidates));
        }

        if (response == null) {
            throw new IllegalStateException("Empty response from Gemini");
        }

        JsonNode contentNode = response.path("candidates").path(0).path("content").path("parts").path(0).path("text");
        if (contentNode.isMissingNode() || contentNode.asText().isBlank()) {
            throw new IllegalStateException("Gemini response is missing content");
        }

        return contentNode.asText();
    }

    private JsonNode generateContent(String model, String apiKey, Map<String, Object> payload) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + model
                + ":generateContent?key="
                + apiKey;

        return webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }

    private AiPlanResponse parseResponse(String rawContent, String destination, Double budget, Integer duration, List<String> interests) {
        String cleaned = cleanupJson(rawContent);

        JsonNode root;
        try {
            root = objectMapper.readTree(cleaned);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("LLM response is not valid JSON");
        }

        AiPlanResponse response = new AiPlanResponse();
        response.setDestination(readText(root, "destination", destination));
        response.setEstimatedCost(readDouble(root, "estimatedCost", budget == null ? 0.0 : budget));
        response.setItinerary(readStringList(root.path("itinerary"), duration == null ? 1 : duration, destination, interests));
        response.setHotels(readHotels(root.path("hotels"), destination));
        response.setAttractions(readAttractions(root.path("attractions"), destination));
        return response;
    }

    private String cleanupJson(String content) {
        String trimmed = content == null ? "" : content.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replace("```json", "").replace("```", "").trim();
        }

        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            return trimmed;
        }

        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return trimmed.substring(start, end + 1);
        }

        throw new IllegalStateException("LLM response does not include JSON object");
    }

    private String readText(JsonNode root, String key, String fallback) {
        JsonNode node = root.path(key);
        if (node.isMissingNode() || node.asText().isBlank()) {
            return fallback;
        }
        return node.asText().trim();
    }

    private Double readDouble(JsonNode root, String key, Double fallback) {
        JsonNode node = root.path(key);
        if (node.isMissingNode() || node.isNull()) {
            return fallback;
        }
        return node.asDouble(fallback);
    }

    private List<String> readStringList(JsonNode node, int duration, String destination, List<String> interests) {
        List<String> items = new ArrayList<>();
        if (node.isArray()) {
            for (JsonNode element : node) {
                String value = element.asText().trim();
                if (!value.isBlank()) {
                    items.add(value);
                }
            }
        }

        int targetDays = Math.max(1, duration);
        while (items.size() < targetDays) {
            int day = items.size() + 1;
            String focus = interests != null && !interests.isEmpty() ? interests.get((day - 1) % interests.size()) : "culture";
            items.add("Day " + day + ": Explore " + destination + " with focus on " + focus + " and local experiences.");
        }

        if (items.size() > targetDays) {
            return items.subList(0, targetDays);
        }
        return items;
    }

    private List<AiPlanResponse.HotelDto> readHotels(JsonNode node, String destination) {
        List<AiPlanResponse.HotelDto> hotels = new ArrayList<>();
        if (node.isArray()) {
            for (JsonNode item : node) {
                AiPlanResponse.HotelDto hotel = new AiPlanResponse.HotelDto();
                hotel.setName(readText(item, "name", destination + " Hotel"));
                hotel.setLocation(readText(item, "location", destination));
                hotel.setPrice(readDouble(item, "price", 100.0));
                hotel.setRating(readDouble(item, "rating", 4.2));
                hotels.add(hotel);
            }
        }

        if (hotels.size() >= 3) {
            return hotels.subList(0, 3);
        }

        while (hotels.size() < 3) {
            AiPlanResponse.HotelDto fallback = new AiPlanResponse.HotelDto();
            int idx = hotels.size() + 1;
            fallback.setName(destination + " Stay " + idx);
            fallback.setLocation(destination);
            fallback.setPrice(90.0 + (idx * 20));
            fallback.setRating(4.0 + (idx * 0.1));
            hotels.add(fallback);
        }

        return hotels;
    }

    private List<AiPlanResponse.AttractionDto> readAttractions(JsonNode node, String destination) {
        List<AiPlanResponse.AttractionDto> attractions = new ArrayList<>();
        if (node.isArray()) {
            for (JsonNode item : node) {
                AiPlanResponse.AttractionDto attraction = new AiPlanResponse.AttractionDto();
                attraction.setName(readText(item, "name", destination + " Highlight"));
                attraction.setCategory(readText(item, "category", "culture"));
                attraction.setLocation(readText(item, "location", destination));
                attraction.setRating(readDouble(item, "rating", 4.3));
                attractions.add(attraction);
            }
        }

        if (attractions.size() >= 4) {
            return attractions.subList(0, Math.min(6, attractions.size()));
        }

        while (attractions.size() < 4) {
            AiPlanResponse.AttractionDto fallback = new AiPlanResponse.AttractionDto();
            int idx = attractions.size() + 1;
            fallback.setName(destination + " Attraction " + idx);
            fallback.setCategory(idx % 2 == 0 ? "food" : "culture");
            fallback.setLocation(destination);
            fallback.setRating(4.0 + (idx * 0.1));
            attractions.add(fallback);
        }

        return attractions;
    }

    private String buildPrompt(String destination, Double budget, Integer duration, List<String> interests) {
        String interestsText = (interests == null || interests.isEmpty()) ? "culture, food, nature" : String.join(", ", interests);
        return "Create a practical travel plan in strict JSON.\n"
                + "Destination: " + destination + "\n"
                + "Budget: " + budget + "\n"
                + "Duration days: " + duration + "\n"
                + "Interests: " + interestsText + "\n\n"
                + "Return ONLY JSON with keys: destination, estimatedCost, itinerary, hotels, attractions.\n"
                + "itinerary must be an array with exactly " + duration + " day-wise items.\n"
                + "hotels must include at least 3 objects with fields name, location, price, rating.\n"
                + "attractions must include at least 4 objects with fields name, category, location, rating.\n"
                + "The destination value in output must stay exactly: " + destination + ".";
    }

    private AiPlanResponse buildFallbackPlan(String destination, Double budget, Integer duration, List<String> interests, String reason) {
        int days = duration == null || duration < 1 ? 1 : duration;
        double totalBudget = budget == null || budget <= 0 ? 1500.0 : budget;

        List<String> itinerary = new ArrayList<>();
        List<String> safeInterests = (interests == null || interests.isEmpty())
                ? List.of("culture", "food", "nature")
                : interests;

        for (int day = 1; day <= days; day++) {
            String focus = safeInterests.get((day - 1) % safeInterests.size());
            itinerary.add("Day " + day + ": Explore " + destination + " with focus on " + focus + " and local experiences.");
        }

        AiPlanResponse.HotelDto h1 = new AiPlanResponse.HotelDto();
        h1.setName(destination + " Central Stay");
        h1.setLocation(destination + " Center");
        h1.setPrice(Math.max(35, (totalBudget / days) * 0.22));
        h1.setRating(4.2);

        AiPlanResponse.HotelDto h2 = new AiPlanResponse.HotelDto();
        h2.setName(destination + " Comfort Inn");
        h2.setLocation(destination + " Downtown");
        h2.setPrice(Math.max(45, (totalBudget / days) * 0.27));
        h2.setRating(4.3);

        AiPlanResponse.HotelDto h3 = new AiPlanResponse.HotelDto();
        h3.setName(destination + " Premium Suites");
        h3.setLocation(destination + " Prime District");
        h3.setPrice(Math.max(60, (totalBudget / days) * 0.34));
        h3.setRating(4.5);

        List<AiPlanResponse.AttractionDto> attractions = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            String focus = safeInterests.get(i % safeInterests.size());
            AiPlanResponse.AttractionDto attraction = new AiPlanResponse.AttractionDto();
            attraction.setName(destination + " " + capitalize(focus) + " Spot " + (i + 1));
            attraction.setCategory(focus.toLowerCase());
            attraction.setLocation(destination);
            attraction.setRating(4.2 + (i * 0.1));
            attractions.add(attraction);
        }

        AiPlanResponse response = new AiPlanResponse();
        response.setDestination(destination + " (fallback)");
        response.setEstimatedCost(Math.round(totalBudget * 0.85 * 100.0) / 100.0);
        response.setItinerary(itinerary);
        response.setHotels(List.of(h1, h2, h3));
        response.setAttractions(attractions);
        return response;
    }

    private String capitalize(String text) {
        if (text == null || text.isBlank()) {
            return "Culture";
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }
}
