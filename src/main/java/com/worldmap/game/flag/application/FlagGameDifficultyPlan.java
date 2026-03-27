package com.worldmap.game.flag.application;

public record FlagGameDifficultyPlan(
	String label,
	String guide,
	int candidatePoolSize,
	boolean stableContinentTargetsOnly
) {
}
