package com.projects.adaptlearn.repository;

import com.projects.adaptlearn.model.StudyPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StudyPlanRepository extends JpaRepository<StudyPlan, Long> {

    List<StudyPlan> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<StudyPlan> findFirstByUserIdOrderByCreatedAtDesc(Long userId);


}