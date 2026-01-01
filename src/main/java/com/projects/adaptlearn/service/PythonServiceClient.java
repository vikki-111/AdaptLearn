package com.projects.adaptlearn.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PythonServiceClient {
    private final RestTemplate restTemplate = new RestTemplate();
    public void triggerPlanGeneration(Long userId) {
        // We just send the ID, and Python can fetch what it needs from the DB
        // OR we can send a JSON of weak areas.
        try {
            String PYTHON_URL = "http://localhost:5000/generate-plan";
            restTemplate.postForEntity(PYTHON_URL, userId, String.class);
        } catch (Exception e) {
            System.out.println("AI Service is down, but progress will be saved!");
        }
    }
}