package com.worldmap.game.capital.web;

import jakarta.validation.constraints.Size;

public record StartCapitalGameRequest(
	@Size(max = 20, message = "nickname은 20자 이하여야 합니다.")
	String nickname
) {
}
