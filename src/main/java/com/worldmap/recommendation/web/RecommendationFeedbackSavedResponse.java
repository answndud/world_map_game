package com.worldmap.recommendation.web;

public record RecommendationFeedbackSavedResponse(
	Long feedbackId,
	Integer satisfactionScore,
	String surveyVersion,
	String engineVersion
) {
}
