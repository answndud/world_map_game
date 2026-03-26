package com.worldmap.game.population.application;

import com.worldmap.game.population.domain.PopulationGameStageStatus;
import java.time.LocalDateTime;
import java.util.List;

public record PopulationGameStageResultView(
	Integer stageNumber,
	String targetCountryName,
	Integer populationYear,
	Long targetPopulation,
	String correctOptionLabel,
	PopulationGamePrecisionBand precisionBand,
	PopulationGameStageStatus status,
	Integer attemptCount,
	Integer awardedScore,
	LocalDateTime clearedAt,
	List<PopulationGameAttemptResultView> attempts
) {
}
