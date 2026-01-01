package com.projects.adaptlearn.service;

import com.projects.adaptlearn.model.StudyPlan;
import com.projects.adaptlearn.repository.StudyPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class StudyPlanService {
    private final StudyPlanRepository studyPlanRepository;
    private final RestTemplate restTemplate; // We'll need a Config bean for this

    public void requestNewPlanFromAI(Long userId) {
        String pythonApiUrl = "http://localhost:5000/generate-plan/" + userId;
        //TODO: Ping python service
        try {
            restTemplate.postForLocation(pythonApiUrl, null);
        } catch (Exception e) {
            //TODO: Log that AI is down, but don't crash the Java app
        }
    }
}