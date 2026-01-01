package com.projects.adaptlearn.repository;

import com.projects.adaptlearn.model.AssessmentStatus;
import com.projects.adaptlearn.model.StudyPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StudyPlanRepository extends JpaRepository<StudyPlan, Long> {

    Optional<StudyPlan> findByUserIdAndStatus(Long userId, AssessmentStatus status);
}