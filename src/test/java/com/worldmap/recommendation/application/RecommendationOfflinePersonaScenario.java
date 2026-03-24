package com.worldmap.recommendation.application;

import com.worldmap.recommendation.domain.RecommendationSurveyAnswers;
import java.util.List;

record RecommendationOfflinePersonaScenario(
	String id,
	String description,
	RecommendationSurveyAnswers answers,
	List<String> expectedCandidates,
	String expectedSatisfactionRange
) {
}
