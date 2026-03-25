package com.worldmap.recommendation.application;

import com.worldmap.recommendation.domain.RecommendationSurveyAnswers;
import java.util.List;

public record RecommendationPersonaBaselineScenario(
	String id,
	String description,
	RecommendationSurveyAnswers answers,
	List<String> expectedCandidates,
	String expectedSatisfactionRange,
	String analysisNote,
	boolean activeSignal
) {
}
