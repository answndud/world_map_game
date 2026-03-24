package com.worldmap.recommendation.application;

import java.time.LocalDateTime;

public record RecommendationFeedbackSummaryView(
	String surveyVersion,
	String engineVersion,
	long responseCount,
	double averageSatisfaction,
	long score1Count,
	long score2Count,
	long score3Count,
	long score4Count,
	long score5Count,
	LocalDateTime lastSubmittedAt
) {
}
