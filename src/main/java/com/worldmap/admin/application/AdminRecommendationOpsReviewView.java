package com.worldmap.admin.application;

import java.util.List;

public record AdminRecommendationOpsReviewView(
	int currentVersionResponseCount,
	double currentVersionAverageSatisfaction,
	String currentSurveyVersion,
	String currentEngineVersion,
	int baselineMatchedScenarioCount,
	int baselineTotalScenarioCount,
	int weakScenarioCount,
	int anchorDriftScenarioCount,
	String priorityActionTitle,
	String priorityActionReason,
	List<String> priorityScenarioIds
) {
}
