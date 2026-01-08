package com.projects.adaptlearn.service;

import org.springframework.beans.factory.annotation.Value;
import com.projects.adaptlearn.model.Question;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PythonServiceClient {

    private final RestTemplate restTemplate;
    @Value("${AI_SERVICE_URL:http://127.0.0.1:5000}")
    private String PYTHON_API_URL;
    public List<Question> fetchAiQuestions(String topic) {
        String url = PYTHON_API_URL + "/generate-questions";
        System.out.println(">>> [JAVA] Fetching questions: " + topic);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("topic", topic);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            List<Map<String, Object>> response = restTemplate.postForObject(url, entity, List.class);

            System.out.println(">>> [JAVA] Parsing questions...");
            List<Question> questions = new ArrayList<>();

            if (response != null && !response.isEmpty()) {
                for (Map<String, Object> data : response) {
                    Question q = new Question();
                    q.setQuestionText((String) data.get("questionText"));

                    Object optionsObj = data.get("options");
                    if (optionsObj instanceof List) {
                        q.setOptions((List<String>) optionsObj);
                    } else if (optionsObj != null) {
                        q.setOptions(new ArrayList<>());
                    }

                    q.setCorrectAnswer((String) data.get("correctAnswer"));
                    questions.add(q);
                }
                System.out.println(">>> [JAVA] Parsed " + questions.size() + " questions");
            } else {
                System.err.println(">>> [JAVA] Empty response");
            }

            return questions;

        } catch (org.springframework.web.client.RestClientException e) {
            System.err.println(">>> [JAVA] Connection error: " + e.getMessage());
            throw new RuntimeException("Failed to connect to Python AI service. Make sure AI_SERVICE_URL is configured correctly or the service is running.", e);
        } catch (Exception e) {
            System.err.println(">>> [JAVA] Error: " + e.getMessage());
            throw new RuntimeException("Failed to generate questions from AI service: " + e.getMessage(), e);
        }
    }
    public String fetchStudyPlan(List<String> weakAreas) {
        return fetchPersonalizedStudyPlan(weakAreas, "Basic study plan request", 0.0);
    }

    public String fetchPersonalizedStudyPlan(List<String> weakAreas, String detailedAnalysis, double score) {
        String url = PYTHON_API_URL + "/generate-personalized-study-plan";
        System.out.println(">>> [JAVA] Requesting study plan for: " + weakAreas);

        Map<String, Object> request = new HashMap<>();
        request.put("weakAreas", weakAreas);
        request.put("detailedAnalysis", detailedAnalysis);
        request.put("overallScore", score);

        try {
            Map<String, String> response = restTemplate.postForObject(url, request, Map.class);

            if (response != null && response.containsKey("plan_content")) {
                return response.get("plan_content");
            }
            return generateFallbackPlan(weakAreas, score);

        } catch (Exception e) {
            System.err.println(">>> [JAVA] Plan generation error: " + e.getMessage());
            return generateFallbackPlan(weakAreas, score);
        }
    }

    private String generateFallbackPlan(List<String> weakAreas, double score) {
        StringBuilder plan = new StringBuilder();
        plan.append("# Study Plan\n\n");
        plan.append(String.format("**Score: %.1f%%**\n\n", score));

        plan.append("## Focus Areas:\n");
        for (String area : weakAreas) {
            plan.append(String.format("- **%s**: Review and practice\n", area));
        }

        plan.append("\n## Actions:\n");
        plan.append("1. Review fundamentals\n");
        plan.append("2. Practice exercises\n");
        plan.append("3. Take notes\n");
        plan.append("4. Re-test progress\n");
        plan.append("5. Seek help if needed\n");

        return plan.toString();
    }

}