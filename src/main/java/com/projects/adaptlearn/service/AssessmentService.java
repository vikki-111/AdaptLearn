package com.projects.adaptlearn.service;

import com.projects.adaptlearn.model.*;
import com.projects.adaptlearn.repository.AssessmentRepository;
import com.projects.adaptlearn.repository.UserProgressRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssessmentService {

    private final AssessmentRepository assessmentRepository;
    private final ProgressService progressService; // Clean injection
    private final PythonServiceClient pythonServiceClient;

    @Transactional
    public Assessment submitAssessment(User user, List<Question> questions, BigDecimal score) {
        Assessment assessment = new Assessment();
        assessment.setUser(user);
        assessment.setOverallScore(score);
        assessment.setStartedAt(java.time.LocalDateTime.now().minusMinutes(15));

        Assessment saved = assessmentRepository.save(assessment);
        questions.stream()
                .map(Question::getTopic)
                .distinct()
                .forEach(topic -> progressService.updateTopicMastery(user, topic, score));

        if (score.doubleValue() < 60.0) {
            pythonServiceClient.triggerPlanGeneration(user.getId());
        }

        return saved;
    }
}