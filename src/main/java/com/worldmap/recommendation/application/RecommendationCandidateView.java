package com.worldmap.recommendation.application;

import java.util.List;

public record RecommendationCandidateView(
	int rank,
	String iso3Code,
	String countryNameKr,
	String countryNameEn,
	String continentLabel,
	String capitalCity,
	String populationLabel,
	int matchScore,
	int strongSignalCount,
	int exactMatchCount,
	String headline,
	List<String> reasons
) {
}
