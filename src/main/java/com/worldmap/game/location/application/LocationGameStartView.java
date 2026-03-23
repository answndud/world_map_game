package com.worldmap.game.location.application;

import com.worldmap.game.common.domain.GameSessionStatus;
import java.util.UUID;

public record LocationGameStartView(
	UUID sessionId,
	String playerNickname,
	GameSessionStatus status,
	Integer totalStages,
	Integer livesRemaining,
	String playPageUrl
) {
}
