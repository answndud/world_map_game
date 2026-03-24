package com.worldmap.recommendation.application;

public record RecommendationFeedbackPayloadView(
	String surveyVersion,
	String engineVersion,
	String climatePreference,
	String pacePreference,
	String budgetPreference,
	String environmentPreference,
	String englishImportance,
	String priorityFocus,
	String settlementPreference,
	String mobilityPreference
) {
}
