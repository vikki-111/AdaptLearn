package com.projects.adaptlearn.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final RestTemplate restTemplate;

    @Value("${ai.service.url:http://localhost:5000}")
    private String aiServiceUrl;

    public String processMessage(String message, String userEmail) {
        try {
            // Prepare the request for the Python AI service
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> requestBody = Map.of(
                "message", message,
                "user_email", userEmail
            );

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

            // Call the Python AI service chat endpoint
            ResponseEntity<Map> response = restTemplate.exchange(
                aiServiceUrl + "/chat",
                HttpMethod.POST,
                entity,
                Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Object reply = response.getBody().get("reply");
                return reply != null ? reply.toString() : "I received your message but couldn't generate a response.";
            } else {
                return "Sorry, I'm having trouble connecting to the AI service right now.";
            }

        } catch (Exception e) {
            // Log the error and return a user-friendly message
            System.err.println("Error processing chat message: " + e.getMessage());
            return "I'm sorry, I encountered an error while processing your message. Please try again.";
        }
    }
}


