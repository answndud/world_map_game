package com.worldmap.recommendation.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendationFeedbackRepository extends JpaRepository<RecommendationFeedback, Long> {
}
