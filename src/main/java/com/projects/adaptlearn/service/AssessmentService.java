package com.projects.adaptlearn.service;

import com.projects.adaptlearn.model.*;
import com.projects.adaptlearn.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AssessmentService {

    private final QuestionRepository questionRepository;
    private final AssessmentRepository assessmentRepository;
    private final TopicRepository topicRepository;
    private final StudyPlanRepository studyPlanRepository;
    private final PythonServiceClient pythonClient;
    private final ProgressService progressService;

    public List<Question> getOrGenerateQuestions(String topicName) {
        List<Question> aiQuestions = pythonClient.fetchAiQuestions(topicName);

        if (aiQuestions == null || aiQuestions.isEmpty()) {
            throw new RuntimeException("Failed to generate questions from AI service");
        }

        Topic topic = topicRepository.findByName(topicName)
                .orElseGet(() -> {
                    Topic newTopic = new Topic();
                    newTopic.setName(topicName);
                    newTopic.setDifficulty(Difficulty.MEDIUM);
                    newTopic.setDescription("Generated topic for " + topicName);
                    return topicRepository.save(newTopic);
                });

        aiQuestions.forEach(q -> q.setTopic(topic));

        return aiQuestions;
    }

    @Transactional
    public Assessment submitAssessmentWithAnalysis(User user, BigDecimal score,
                                                  List<Map<String, Object>> detailedResults,
                                                  List<Map<String, Object>> weakAreas) {
        Assessment assessment = new Assessment();
        assessment.setUser(user);
        assessment.setTitle("Java Basics Diagnostic Test");
        assessment.setOverallScore(score);
        assessment.setStartedAt(LocalDateTime.now().minusMinutes(10));
        assessment.setStatus(AssessmentStatus.COMPLETED);

        Assessment saved = assessmentRepository.save(assessment);

        generatePersonalizedStudyPlan(user, detailedResults, weakAreas, score);
        updateUserProgressTracking(user, detailedResults, score);

        return saved;
    }

    private void generatePersonalizedStudyPlan(User user, List<Map<String, Object>> detailedResults,
                                             List<Map<String, Object>> weakAreas, BigDecimal score) {
        try {
            StringBuilder analysisPrompt = new StringBuilder();
            analysisPrompt.append("Student Assessment Analysis:\n");
            analysisPrompt.append(String.format("Overall Score: %.1f%%\n\n", score.doubleValue()));

            if (detailedResults != null && !detailedResults.isEmpty()) {
                analysisPrompt.append("Question-by-Question Analysis:\n");
                for (Map<String, Object> result : detailedResults) {
                    Boolean isCorrect = (Boolean) result.get("isCorrect");
                    String questionText = (String) result.get("questionText");
                    String topic = (String) result.get("topic");

                    analysisPrompt.append(String.format("- %s: %s (Topic: %s)\n",
                        isCorrect ? "✓" : "✗",
                        questionText.substring(0, Math.min(50, questionText.length())),
                        topic));
                }
                analysisPrompt.append("\n");
            }

            if (weakAreas != null && !weakAreas.isEmpty()) {
                analysisPrompt.append("Identified Weak Areas:\n");
                for (Map<String, Object> weakArea : weakAreas) {
                    String topic = (String) weakArea.get("topic");
                    String accuracy = (String) weakArea.get("accuracy");
                    Integer incorrectCount = ((Number) weakArea.get("incorrectCount")).intValue();

                    analysisPrompt.append(String.format("- %s: %.1f%% accuracy (%d incorrect answers)\n",
                        topic, Double.parseDouble(accuracy), incorrectCount));
                }
            } else {
                analysisPrompt.append("No major weak areas identified - student performed well overall!\n");
            }

            analysisPrompt.append("\nCreate a personalized 5-step study plan that addresses their specific weaknesses ");
            analysisPrompt.append("and helps them improve in the areas they struggled with. ");
            analysisPrompt.append("Include specific resources, practice exercises, and learning strategies.");

            List<String> weakAreaTopics = new ArrayList<>();
            if (weakAreas != null && !weakAreas.isEmpty()) {
                for (Map<String, Object> weakArea : weakAreas) {
                    weakAreaTopics.add((String) weakArea.get("topic"));
                }
            } else {
                weakAreaTopics.add("General Skill Enhancement");
            }

            String planContent = pythonClient.fetchPersonalizedStudyPlan(
                weakAreaTopics, analysisPrompt.toString(), score.doubleValue());

            try {
                List<StudyPlan> existingPlans = studyPlanRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
                if (!existingPlans.isEmpty()) {
                    studyPlanRepository.deleteAll(existingPlans);
                }
            } catch (Exception e) {
                System.err.println("Warning: Could not delete existing study plans: " + e.getMessage());
            }

            try {
                StudyPlan plan = new StudyPlan();
                plan.setUser(user);
                plan.setAiRecommendations(planContent);
                plan.setCreatedAt(LocalDateTime.now());
                studyPlanRepository.save(plan);
                System.out.println("Successfully created personalized study plan for user: " + user.getUsername());
            } catch (org.springframework.dao.DataIntegrityViolationException e) {
                System.err.println("Database constraint violation - study plan not created: " + e.getMessage());
                System.err.println("FIX: Run this SQL command:");
                System.err.println("mysql -u root -p adapt_learn -e \"ALTER TABLE study_plans DROP INDEX UK3d8hgibwf9mwyrow9159qadf2;\"");
                System.err.println("Assessment saved successfully, but study plan creation skipped due to database constraint.");
            }

        } catch (Exception e) {
            System.err.println("Error generating personalized study plan: " + e.getMessage());
            try {
                List<String> fallbackAreas = List.of("General Concepts");
                String planContent = pythonClient.fetchStudyPlan(fallbackAreas);

                try {
                    List<StudyPlan> existingPlans = studyPlanRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
                    if (!existingPlans.isEmpty()) {
                        studyPlanRepository.deleteAll(existingPlans);
                    }
                } catch (Exception e1) {
                    System.err.println("Warning: Could not delete existing study plans in fallback: " + e.getMessage());
                }

                StudyPlan plan = new StudyPlan();
                plan.setUser(user);
                plan.setAiRecommendations(planContent);
                plan.setCreatedAt(LocalDateTime.now());
                studyPlanRepository.save(plan);
                System.out.println("Successfully created fallback study plan for user: " + user.getUsername());
            } catch (Exception e2) {
                System.err.println("Fallback study plan creation failed: " + e2.getMessage());
            }
        }
    }

    private void updateUserProgressTracking(User user, List<Map<String, Object>> detailedResults, BigDecimal score) {
        try {
            Map<String, List<Map<String, Object>>> topicResults = new HashMap<>();

            for (Map<String, Object> result : detailedResults) {
                String topic = (String) result.get("topic");
                if (topic == null) topic = "General";

                topicResults.computeIfAbsent(topic, k -> new ArrayList<>()).add(result);
            }

            for (Map.Entry<String, List<Map<String, Object>>> entry : topicResults.entrySet()) {
                String topicName = entry.getKey();
                List<Map<String, Object>> topicQuestions = entry.getValue();

                long correctAnswers = topicQuestions.stream()
                    .mapToLong(q -> ((Boolean) q.get("isCorrect")) ? 1L : 0L)
                    .sum();

                BigDecimal accuracy = BigDecimal.valueOf((double) correctAnswers / topicQuestions.size() * 100);

                Topic topic = topicRepository.findByName(topicName)
                    .orElseGet(() -> {
                        Topic newTopic = new Topic();
                        newTopic.setName(topicName);
                        newTopic.setDifficulty(Difficulty.MEDIUM);
                        return topicRepository.save(newTopic);
                    });

                progressService.updateTopicMastery(user, topic, accuracy);
            }

        } catch (Exception e) {
            System.err.println("Error updating user progress tracking: " + e.getMessage());
        }
    }

    // Keep the old method for backward compatibility
    @Transactional
    public Assessment submitAssessment(User user, List<Question> questions, BigDecimal score) {
        return submitAssessmentWithAnalysis(user, score, null, null);
    }
}