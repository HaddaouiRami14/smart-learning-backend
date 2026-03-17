package com.example.SmartLearning.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.example.SmartLearning.Enum.ProgrammingLanguage;
import org.json.JSONObject;

@Service
@RequiredArgsConstructor
public class JDoodleService {

    @Value("${jdoodle.client-id}")
    private String clientId;

    @Value("${jdoodle.client-secret}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String JDOODLE_API_URL = "https://api.jdoodle.com/v1/execute";

    public String executeCode(ProgrammingLanguage language, String sourceCode, String input) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            JSONObject requestBody = new JSONObject();
            requestBody.put("clientId", clientId);
            requestBody.put("clientSecret", clientSecret);
            requestBody.put("script", sourceCode);
            requestBody.put("stdin", input != null ? input : "");
            requestBody.put("language", getLanguageCode(language));
            requestBody.put("versionIndex", getVersionIndex(language));

            HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);

            ResponseEntity<String> response = restTemplate.postForEntity(JDOODLE_API_URL, request, String.class);

            // Log pour debug — peut être supprimé en production
            System.out.println("JDoodle raw response: " + response.getBody());

            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Error executing code: " + e.getMessage(), e);
        }
    }

    public JSONObject parseResult(String resultJson) {
        JSONObject result = new JSONObject(resultJson);
        JSONObject parsed = new JSONObject();

        String output = result.optString("output", "").trim();
        boolean isExecutionSuccess = result.optBoolean("isExecutionSuccess", false);
        boolean isCompiled = result.optBoolean("isCompiled", true);

        // cpuTime peut être null dans la réponse JDoodle → parser en String d'abord
        double cpuTime = 0.0;
        try {
            String cpuTimeStr = result.optString("cpuTime", "0");
            if (cpuTimeStr != null && !cpuTimeStr.equals("null") && !cpuTimeStr.isEmpty()) {
                cpuTime = Double.parseDouble(cpuTimeStr);
            }
        } catch (NumberFormatException ignored) {}

        if (!isCompiled) {
            // Erreur de compilation (Java, etc.)
            parsed.put("status_id", 6);
            parsed.put("status_description", "Compilation Error");
            parsed.put("stdout", "");
            parsed.put("stderr", output); // JDoodle met l'erreur dans output
            parsed.put("compile_output", output);

        } else if (!isExecutionSuccess) {
            // Erreur d'exécution : RuntimeError, NameError, TypeError, etc.
            parsed.put("status_id", 11);
            parsed.put("status_description", "Runtime Error");
            parsed.put("stdout", "");
            parsed.put("stderr", output); // JDoodle met le traceback dans output
            parsed.put("compile_output", "");

        } else {
            // Exécution réussie
            parsed.put("status_id", 3);
            parsed.put("status_description", "Accepted");
            parsed.put("stdout", output);
            parsed.put("stderr", "");
            parsed.put("compile_output", "");
        }

        parsed.put("time", cpuTime);
        return parsed;
    }

    private String getLanguageCode(ProgrammingLanguage language) {
        switch (language) {
            case PYTHON:     return "python3";
            case JAVASCRIPT: return "nodejs";
            case JAVA:       return "java";
            default:         return "python3";
        }
    }

    private String getVersionIndex(ProgrammingLanguage language) {
        switch (language) {
            case PYTHON:     return "4"; // Python 3.11
            case JAVASCRIPT: return "4"; // Node.js 18
            case JAVA:       return "4"; // Java 17
            default:         return "0";
        }
    }
}