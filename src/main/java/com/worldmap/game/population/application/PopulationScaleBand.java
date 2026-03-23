package com.worldmap.game.population.application;

public record PopulationScaleBand(
	Long lowerBoundInclusive,
	Long upperBoundExclusive,
	String label
) {

	public boolean contains(Long population) {
		if (population == null) {
			return false;
		}

		if (upperBoundExclusive == null) {
			return population >= lowerBoundInclusive;
		}

		return population >= lowerBoundInclusive && population < upperBoundExclusive;
	}
}
