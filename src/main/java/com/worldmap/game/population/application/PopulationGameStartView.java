package com.worldmap.game.population.application;

import com.worldmap.game.common.domain.GameSessionStatus;
import com.worldmap.game.population.domain.PopulationGameLevel;
import java.util.UUID;

public record PopulationGameStartView(
	UUID sessionId,
	String playerNickname,
	GameSessionStatus status,
	PopulationGameLevel gameLevel,
	Integer totalStages,
	Integer livesRemaining,
	String playPageUrl
) {
}
