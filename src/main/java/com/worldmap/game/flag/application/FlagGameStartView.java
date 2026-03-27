package com.worldmap.game.flag.application;

import com.worldmap.game.common.domain.GameSessionStatus;
import java.util.UUID;

public record FlagGameStartView(
	UUID sessionId,
	String playerNickname,
	GameSessionStatus status,
	Integer totalStages,
	Integer livesRemaining,
	String playPageUrl
) {
}
