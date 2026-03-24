package com.worldmap.recommendation.application;

import com.worldmap.recommendation.domain.RecommendationSurveyAnswers;

public record RecommendationFeedbackSubmission(
	String surveyVersion,
	String engineVersion,
	Integer satisfactionScore,
	RecommendationSurveyAnswers answers
) {
}
