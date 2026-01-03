package com.projects.adaptlearn.service;

import com.projects.adaptlearn.model.StudyPlan;
import com.projects.adaptlearn.model.User;
import com.projects.adaptlearn.repository.StudyPlanRepository;
import com.projects.adaptlearn.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StudyPlanService {

    private final StudyPlanRepository studyPlanRepository;
    private final UserRepository userRepository;
    private final PythonServiceClient pythonClient; // Use the client we built!

    public void generateAndSavePlan(Long userId, List<String> weakAreas) {
        try {
            String planContent = pythonClient.fetchStudyPlan(weakAreas);

            // 2. Find the user
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // 3. Create and Save the StudyPlan entity
            StudyPlan plan = new StudyPlan();
            plan.setUser(user);
            plan.setAiRecommendations(planContent);
            plan.setCreatedAt(LocalDateTime.now());

            studyPlanRepository.save(plan);

        } catch (Exception e) {
            System.err.println("AI Plan Generation Failed: " + e.getMessage());
        }
    }
}