package com.worldmap.recommendation.application;

public record RecommendationFeedbackPayloadView(
	String surveyVersion,
	String engineVersion,
	String climatePreference,
	String seasonTolerance,
	String pacePreference,
	String costQualityPreference,
	String environmentPreference,
	String englishSupportNeed,
	String safetyPriority,
	String publicServicePriority,
	String foodImportance,
	String diversityImportance,
	String settlementPreference,
	String mobilityPreference
) {
}
