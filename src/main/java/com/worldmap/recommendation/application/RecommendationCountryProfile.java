package com.worldmap.recommendation.application;

public record RecommendationCountryProfile(
	String iso3Code,
	int climateValue,
	int paceValue,
	int priceLevel,
	int urbanityValue,
	int englishSupport,
	int safety,
	int welfare,
	int food,
	int diversity,
	String hookLine
) {
}
