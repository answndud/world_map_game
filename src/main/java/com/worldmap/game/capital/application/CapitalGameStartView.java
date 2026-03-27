package com.worldmap.game.capital.application;

import com.worldmap.game.common.domain.GameSessionStatus;
import java.util.UUID;

public record CapitalGameStartView(
	UUID sessionId,
	String playerNickname,
	GameSessionStatus status,
	Integer totalStages,
	Integer livesRemaining,
	String playPageUrl
) {
}
