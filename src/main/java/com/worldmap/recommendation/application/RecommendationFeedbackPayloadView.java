package com.worldmap.recommendation.application;

public record RecommendationFeedbackPayloadView(
	String surveyVersion,
	String engineVersion,
	String climatePreference,
	String seasonStylePreference,
	String seasonTolerance,
	String pacePreference,
	String crowdPreference,
	String costQualityPreference,
	String housingPreference,
	String environmentPreference,
	String mobilityPreference,
	String englishSupportNeed,
	String newcomerSupportNeed,
	String safetyPriority,
	String publicServicePriority,
	String digitalConveniencePriority,
	String foodImportance,
	String diversityImportance,
	String cultureLeisureImportance,
	String workLifePreference,
	String settlementPreference,
	String futureBasePreference
) {
}
