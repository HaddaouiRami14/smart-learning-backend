package com.example.SmartLearning.chatbot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
 
import java.util.*;
 
@Component
@Slf4j
@RequiredArgsConstructor
public class OpenRouterClient {
 
    @Value("${openrouter.api.key}")
    private String apiKey;
 
    
    @Value("${openrouter.model}")
    private String model;
 
    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";
 
    private final RestTemplate  restTemplate = new RestTemplate();
    private final ObjectMapper  objectMapper = new ObjectMapper();
 
    /**
     * Envoie un échange (systemPrompt + message utilisateur) à OpenRouter
     * et retourne la réponse textuelle du modèle.
     */
    public String chat(String systemPrompt, String userMessage) {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        headers.set("HTTP-Referer", "https://smartlearning.app");  // requis par OpenRouter
        headers.set("X-Title", "SmartLearning");
 
        var body = Map.of(
            "model", model,
            "messages", List.of(
                Map.of("role", "system",  "content", systemPrompt),
                Map.of("role", "user",    "content", userMessage)
            ),
            "temperature", 0.7,
            "max_tokens",  800
        );
 
        try {
            var request  = new HttpEntity<>(body, headers);
            var response = restTemplate.postForEntity(API_URL, request, String.class);
 
            var json    = objectMapper.readTree(response.getBody());
            return json.at("/choices/0/message/content").asText("");
 
        } catch (Exception e) {
            log.error("OpenRouter API error: {}", e.getMessage());
            return null;
        }
    }
}
