package com.projects.adaptlearn.controller;

import com.projects.adaptlearn.model.Assessment;
import com.projects.adaptlearn.model.Question;
import com.projects.adaptlearn.model.User;
import com.projects.adaptlearn.repository.QuestionRepository;
import com.projects.adaptlearn.service.AssessmentService;
import com.projects.adaptlearn.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assessments")
@RequiredArgsConstructor
public class AssessmentController {

    private final AssessmentService assessmentService;
    private final QuestionRepository questionRepository;
    private final UserService userService;

    // This sends the questions to the frontend
    @GetMapping("/generate")
    public ResponseEntity<List<Question>> getDiagnosticTest() {
        // If DB is empty, it tells the service to fetch/generate some
        List<Question> questions = assessmentService.getOrGenerateQuestions("Java Basics");
        return ResponseEntity.ok(questions);
    }

    // This handles the user's submission
    @PostMapping("/submit")
    public ResponseEntity<?> submit(@AuthenticationPrincipal UserDetails userDetails,
                                    @RequestBody Map<String, Object> payload) {

        // 1. Identify who is submitting
        User user = userService.findByUsername(userDetails.getUsername());

        // 2. Extract score from the JSON payload
        Number scoreNum = (Number) payload.get("score");
        BigDecimal score = BigDecimal.valueOf(scoreNum.doubleValue());

        // 3. Get all questions (to link to the assessment result)
        List<Question> questions = questionRepository.findAll();

        // 4. Let the service handle saving and AI plan generation
        Assessment result = assessmentService.submitAssessment(user, questions, score);

        return ResponseEntity.ok(Map.of(
                "message", "Assessment processed successfully",
                "overallScore", result.getOverallScore(),
                "needsAiPlan", result.getOverallScore().doubleValue() < 60.0
        ));
    }
}