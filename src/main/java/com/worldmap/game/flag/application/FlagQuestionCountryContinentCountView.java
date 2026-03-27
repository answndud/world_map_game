package com.worldmap.game.flag.application;

import com.worldmap.country.domain.Continent;

public record FlagQuestionCountryContinentCountView(
	Continent continent,
	Integer countryCount
) {
}
