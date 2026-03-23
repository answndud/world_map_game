package com.worldmap.game.population.application;

import java.util.List;

public record PopulationRoundOptions(
	List<Long> options,
	Integer correctOptionNumber
) {
}
