package com.worldmap.game.populationbattle.application;

public record PopulationBattleGameDifficultyPlan(
	String label,
	int candidatePoolSize,
	int minimumRankGap,
	int maximumRankGap
) {
}
