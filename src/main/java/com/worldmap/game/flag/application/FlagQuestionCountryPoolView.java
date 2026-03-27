package com.worldmap.game.flag.application;

import java.util.List;

public record FlagQuestionCountryPoolView(
	Integer availableCountryCount,
	List<FlagQuestionCountryContinentCountView> continentCounts,
	List<FlagQuestionCountryView> countries
) {
}
