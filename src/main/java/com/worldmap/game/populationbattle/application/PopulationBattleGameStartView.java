package com.worldmap.game.populationbattle.application;

import com.worldmap.game.common.domain.GameSessionStatus;
import java.util.UUID;

public record PopulationBattleGameStartView(
	UUID sessionId,
	String playerNickname,
	GameSessionStatus status,
	Integer totalStages,
	Integer livesRemaining,
	String playPageUrl
) {
}
