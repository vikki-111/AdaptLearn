package com.projects.adaptlearn.service;

import com.projects.adaptlearn.model.Question;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
@Setter
@Getter
public class PythonServiceClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String PYTHON_BASE_URL = "http://localhost:5000";

    public List<Question> fetchAiQuestions(String topic) {
        String url = PYTHON_BASE_URL + "/generate-questions";
        Map<String, Object> request = Map.of(
                "topic", topic,
                "count", 5
        );

        try {
            List<Map<String, Object>> response = restTemplate.postForObject(url, request, List.class);
            List<Question> questions = new ArrayList<>();

            if (response != null) {
                for (Map<String, Object> data : response) {
                    Question q = new Question();
                    q.setQuestionText((String) data.get("questionText"));
                    q.setOptions((List<String>) data.get("options"));
                    q.setCorrectAnswer((String) data.get("correctAnswer"));
                    questions.add(q);
                }
            }
            return questions;
        } catch (Exception e) {
            System.err.println("Error calling Python AI Service: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public String fetchStudyPlan(List<String> weakAreas) {
        String url = PYTHON_BASE_URL + "/generate-study-plan";
        Map<String, Object> request = Map.of("weakAreas", weakAreas);

        try {
            Map<String, String> response = restTemplate.postForObject(url, request, Map.class);
            return response != null ? response.get("plan_content") : "Plan generation failed.";
        } catch (Exception e) {
            System.err.println("Error fetching Study Plan: " + e.getMessage());
            return "Could not generate AI plan at this time.";
        }
    }
}