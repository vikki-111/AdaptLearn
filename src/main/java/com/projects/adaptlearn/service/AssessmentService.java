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
    private final UserProgressRepository userProgressRepository;
    private final PythonServiceClient pythonServiceClient;

    @Transactional
    public Assessment submitAssessment(User user, List<Question> completedQuestions, List<String> userAnswers) {

        Map<Topic, List<Question>> questionsByTopic = completedQuestions.stream()
                .collect(Collectors.groupingBy(Question::getTopic));

        int totalCorrect = 0;

        for (Map.Entry<Topic, List<Question>> entry : questionsByTopic.entrySet()) {
            Topic topic = entry.getKey();
            List<Question> topicQuestions = entry.getValue();

            long correctInTopic = topicQuestions.stream()
                    .filter(q -> userAnswers.contains(q.getCorrectAnswer())) // Simplified for MVP
                    .count();

            BigDecimal topicScore = BigDecimal.valueOf((double) correctInTopic / topicQuestions.size() * 100);


            updateTopicMastery(user, topic, topicScore);
            totalCorrect += correctInTopic;
        }

        BigDecimal overallScore = BigDecimal.valueOf((double) totalCorrect / completedQuestions.size() * 100);

        Assessment assessment = new Assessment();
        assessment.setUser(user);
        assessment.setOverallScore(overallScore);
        assessment.setStartedAt(LocalDateTime.now().minusMinutes(10));
        if (overallScore.doubleValue() < 60.0) {
            pythonServiceClient.triggerPlanGeneration(user.getId());
        }
        return assessmentRepository.save(assessment);
    }

    private void updateTopicMastery(User user, Topic topic, BigDecimal newScore) {
        UserProgress progress = userProgressRepository.findByUserIdAndTopicId(user.getId(), topic.getId())
                .orElse(new UserProgress());

        progress.setUser(user);
        progress.setTopic(topic);
        progress.setMasteryScore(newScore);
        userProgressRepository.save(progress);
    }
}
