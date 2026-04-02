package com.worldmap.recommendation.application;

import com.worldmap.recommendation.domain.RecommendationSurveyAnswers;

public record RecommendationFeedbackContext(
	String surveyVersion,
	String engineVersion,
	RecommendationSurveyAnswers answers
) {
}
