package com.worldmap.recommendation.application;

import java.util.List;

public record RecommendationSurveyResultView(
	String surveyVersion,
	String engineVersion,
	RecommendationFeedbackPayloadView feedbackPayload,
	List<RecommendationPreferenceSummaryView> submittedPreferences,
	List<RecommendationCandidateView> recommendations
) {
}
