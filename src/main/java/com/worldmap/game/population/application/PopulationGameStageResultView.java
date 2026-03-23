package com.worldmap.game.population.application;

import com.worldmap.game.population.domain.PopulationGameStageStatus;
import java.time.LocalDateTime;
import java.util.List;

public record PopulationGameStageResultView(
	Integer stageNumber,
	String targetCountryName,
	Integer populationYear,
	Long targetPopulation,
	PopulationGameStageStatus status,
	Integer attemptCount,
	Integer awardedScore,
	LocalDateTime clearedAt,
	List<PopulationGameAttemptResultView> attempts
) {
}
