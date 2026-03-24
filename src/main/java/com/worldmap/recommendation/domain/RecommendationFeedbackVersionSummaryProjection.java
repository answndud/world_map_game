package com.worldmap.recommendation.domain;

import java.time.LocalDateTime;

public interface RecommendationFeedbackVersionSummaryProjection {

	String getSurveyVersion();

	String getEngineVersion();

	Long getResponseCount();

	Double getAverageSatisfaction();

	Long getScore1Count();

	Long getScore2Count();

	Long getScore3Count();

	Long getScore4Count();

	Long getScore5Count();

	LocalDateTime getLastSubmittedAt();
}
