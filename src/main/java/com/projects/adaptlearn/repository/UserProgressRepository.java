package com.projects.adaptlearn.repository;

import com.projects.adaptlearn.model.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {

    List<UserProgress> findByUserId(Long userId);
    Optional<UserProgress> findByUserIdAndTopicId(Long userId, Long topicId);
    List<UserProgress> findByUserIdAndMasteryScoreLessThan(Long userId, BigDecimal threshold);
}