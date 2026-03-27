package com.worldmap.game.populationbattle.application;

import com.worldmap.game.populationbattle.domain.PopulationBattleGameStageStatus;
import java.time.LocalDateTime;
import java.util.List;

public record PopulationBattleGameStageResultView(
	Integer stageNumber,
	String questionPrompt,
	String optionOneCountryName,
	Long optionOnePopulation,
	String optionTwoCountryName,
	Long optionTwoPopulation,
	String correctCountryName,
	PopulationBattleGameStageStatus status,
	Integer attemptCount,
	Integer awardedScore,
	LocalDateTime clearedAt,
	List<PopulationBattleGameAttemptResultView> attempts
) {
}
