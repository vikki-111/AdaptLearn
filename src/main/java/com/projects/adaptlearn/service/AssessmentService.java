package com.projects.adaptlearn.service;

import com.projects.adaptlearn.model.*;
import com.projects.adaptlearn.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssessmentService {

    private final QuestionRepository questionRepository;
    private final AssessmentRepository assessmentRepository;
    private final TopicRepository topicRepository;
    private final StudyPlanRepository studyPlanRepository;
    private final PythonServiceClient pythonClient;

    public List<Question> getOrGenerateQuestions(String topicName) {
        Topic topic = topicRepository.findByName(topicName)
                .orElseGet(() -> {
                    Topic newTopic = new Topic();
                    newTopic.setName(topicName);
                    return topicRepository.save(newTopic);
                });

        List<Question> aiQuestions = pythonClient.fetchAiQuestions(topicName);

        aiQuestions.forEach(q -> q.setTopic(topic));
        return questionRepository.saveAll(aiQuestions);
    }

    @Transactional
    public Assessment submitAssessment(User user, List<Question> questions, BigDecimal score) {
        Assessment assessment = new Assessment();
        assessment.setUser(user);
        assessment.setOverallScore(score);
        Assessment saved = assessmentRepository.save(assessment);

        if (score.doubleValue() < 60.0) {
            String planContent = pythonClient.fetchStudyPlan(List.of("General Concepts"));

            StudyPlan plan = new StudyPlan();
            plan.setUser(user);
            plan.setCreatedAt(LocalDateTime.now());
            studyPlanRepository.save(plan);
        }

        return saved;
    }
}