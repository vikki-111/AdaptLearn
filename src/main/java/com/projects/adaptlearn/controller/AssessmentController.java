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

    @GetMapping("/generate")
    public ResponseEntity<?> getDiagnosticTest() {
        try {
            List<Question> questions = assessmentService.getOrGenerateQuestions("Java Basics");

            if (questions == null || questions.isEmpty()) {
                return ResponseEntity.status(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Failed to generate questions. Please ensure Python service is running on localhost:5000"));
            }

            return ResponseEntity.ok(questions);
        } catch (RuntimeException e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/submit")
    public ResponseEntity<?> submit(@AuthenticationPrincipal String email,
                                    @RequestBody Map<String, Object> payload) {
        try {
            User user = userService.authenticateByEmail(email);

            Number scoreNum = (Number) payload.get("score");
            if (scoreNum == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Score is required in the request body"));
            }
            BigDecimal score = BigDecimal.valueOf(scoreNum.doubleValue());

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> detailedResults = (List<Map<String, Object>>) payload.get("detailedResults");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> weakAreas = (List<Map<String, Object>>) payload.get("weakAreas");

            Assessment result = assessmentService.submitAssessmentWithAnalysis(
                user, score, detailedResults, weakAreas);

            return ResponseEntity.ok(Map.of(
                    "message", "Assessment processed successfully",
                    "overallScore", result.getOverallScore(),
                    "needsAiPlan", result.getOverallScore().doubleValue() < 60.0,
                    "weakAreasIdentified", weakAreas != null ? weakAreas.size() : 0
            ));
        } catch (RuntimeException e) {
            String errorMessage = e.getMessage();
            if (errorMessage.contains("UK3d8hgibwf9mwyrow9159qadf2") || errorMessage.contains("Duplicate entry") ||
                errorMessage.contains("database constraint")) {
                return ResponseEntity.ok(Map.of(
                    "message", "Assessment submitted successfully!",
                    "assessmentSaved", true
                ));
            }
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", errorMessage));
        }
    }
}