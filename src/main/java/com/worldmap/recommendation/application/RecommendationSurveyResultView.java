package com.worldmap.recommendation.application;

import java.util.List;

public record RecommendationSurveyResultView(
	List<RecommendationPreferenceSummaryView> submittedPreferences,
	List<RecommendationCandidateView> recommendations
) {
}
