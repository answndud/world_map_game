package com.worldmap.game.population.application;

import java.util.List;
import java.util.UUID;

public record PopulationGameCurrentRoundView(
	UUID sessionId,
	Integer roundNumber,
	Integer totalRounds,
	Integer answeredRoundCount,
	Integer totalScore,
	String targetCountryName,
	Integer populationYear,
	List<PopulationOptionView> options
) {
}
