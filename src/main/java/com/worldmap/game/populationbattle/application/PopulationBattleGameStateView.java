package com.worldmap.game.populationbattle.application;

import com.worldmap.game.common.domain.GameSessionStatus;
import java.util.List;
import java.util.UUID;

public record PopulationBattleGameStateView(
	UUID sessionId,
	Integer stageNumber,
	String difficultyLabel,
	Integer clearedStageCount,
	Integer totalScore,
	Integer livesRemaining,
	String questionPrompt,
	List<PopulationBattleOptionView> options,
	GameSessionStatus gameStatus
) {
}
