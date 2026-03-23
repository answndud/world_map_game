package com.worldmap.game.population.application;

import org.springframework.stereotype.Component;

@Component
public class PopulationOptionLabelFormatter {

	public String labelForPopulation(Long population) {
		return PopulationScaleBandCatalog.resolveByPopulation(population).label();
	}

	public String labelForLowerBound(Long lowerBoundInclusive) {
		return PopulationScaleBandCatalog.resolveByLowerBound(lowerBoundInclusive).label();
	}
}
