package com.worldmap.game.location.application;

import com.worldmap.game.common.domain.GameSessionStatus;
import com.worldmap.game.location.domain.LocationGameLevel;
import java.util.UUID;

public record LocationGameStartView(
	UUID sessionId,
	String playerNickname,
	GameSessionStatus status,
	LocationGameLevel gameLevel,
	Integer totalStages,
	Integer livesRemaining,
	String playPageUrl
) {
}
