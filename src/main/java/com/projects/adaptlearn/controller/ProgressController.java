package com.projects.adaptlearn.controller;

import com.projects.adaptlearn.model.Assessment;
import com.projects.adaptlearn.model.StudyPlan;
import com.projects.adaptlearn.model.User;
import com.projects.adaptlearn.repository.AssessmentRepository;
import com.projects.adaptlearn.repository.StudyPlanRepository;
import com.projects.adaptlearn.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final AssessmentRepository assessmentRepository;
    private final StudyPlanRepository studyPlanRepository;
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<?> getMyProgress(@AuthenticationPrincipal UserDetails userDetails) {

        User user = userService.findByUsername(userDetails.getUsername());

        List<Assessment> history = assessmentRepository.findByUserIdOrderByCompletedAtDesc(user.getId());

        List<StudyPlan> plans = studyPlanRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        return ResponseEntity.ok(Map.of(
                "username", user.getUsername(),
                "assessmentHistory", history,
                "latestPlans", plans
        ));
    }
}