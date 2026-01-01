package com.projects.adaptlearn.service;

import com.projects.adaptlearn.model.Topic;
import com.projects.adaptlearn.model.User;
import com.projects.adaptlearn.model.UserProgress;
import com.projects.adaptlearn.repository.UserProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProgressService {

    private final UserProgressRepository userProgressRepository;

    public void updateTopicMastery(User user, Topic topic, BigDecimal newScore) {
        UserProgress progress = userProgressRepository.findByUserIdAndTopicId(user.getId(), topic.getId())
                .orElse(new UserProgress());

        progress.setUser(user);
        progress.setTopic(topic);
        progress.setMasteryScore(newScore);

        userProgressRepository.save(progress);
    }

    public List<UserProgress> getUserWeakAreas(Long userId) {
        return userProgressRepository.findByUserIdAndMasteryScoreLessThan(userId, BigDecimal.valueOf(60.0));
    }
}