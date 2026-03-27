package com.worldmap.game.capital.application;

import java.util.List;

public record CapitalRoundOptions(
	List<String> options,
	Integer correctOptionNumber
) {
}
