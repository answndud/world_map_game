package com.worldmap.game.population.application;

import com.worldmap.game.common.domain.GameSessionStatus;
import java.util.UUID;

public record PopulationGameStartView(
	UUID sessionId,
	String playerNickname,
	GameSessionStatus status,
	Integer totalRounds,
	String playPageUrl
) {
}
