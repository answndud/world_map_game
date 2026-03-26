package com.worldmap.game.population.web;

import jakarta.validation.constraints.Size;

public record StartPopulationGameRequest(
	@Size(max = 20, message = "nickname은 20자 이하여야 합니다.")
	String nickname,
	String gameLevel
) {
}
