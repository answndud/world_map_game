package com.worldmap.recommendation.application;

public record RecommendationCountryProfile(
	String iso3Code,
	int climateValue,
	int seasonality,
	int paceValue,
	int priceLevel,
	int urbanityValue,
	int englishSupport,
	int safety,
	int welfare,
	int food,
	int diversity,
	int housingSpace,
	int digitalConvenience,
	int cultureScene,
	int newcomerFriendliness,
	String hookLine
) {
}
