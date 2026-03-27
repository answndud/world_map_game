package com.worldmap.game.flag.application;

import java.util.List;

public record FlagRoundOptions(
	List<String> options,
	Integer correctOptionNumber
) {
}
