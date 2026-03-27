package com.worldmap.game.populationbattle.application;

import com.worldmap.country.domain.Country;

public record PopulationBattleRoundOptions(
	Country optionOneCountry,
	Country optionTwoCountry,
	Integer correctOptionNumber
) {
}
