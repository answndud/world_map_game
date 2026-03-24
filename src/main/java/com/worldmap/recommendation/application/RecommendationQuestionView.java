package com.worldmap.recommendation.application;

import java.util.List;

public record RecommendationQuestionView(
	String fieldName,
	String title,
	String helperText,
	List<RecommendationOptionView> options
) {
}
