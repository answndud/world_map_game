package com.worldmap.recommendation.application;

import java.util.List;

public record RecommendationFeedbackInsightsView(
	long totalResponses,
	int trackedVersionCount,
	double overallAverageSatisfaction,
	List<RecommendationFeedbackSummaryView> versionSummaries
) {
}
