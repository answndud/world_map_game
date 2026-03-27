package com.worldmap.game.flag.web;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SubmitFlagAnswerRequest(
	@NotNull(message = "stageNumber는 필수입니다.")
	Integer stageNumber,
	@NotNull(message = "selectedOptionNumber는 필수입니다.")
	@Min(value = 1, message = "selectedOptionNumber는 1 이상이어야 합니다.")
	Integer selectedOptionNumber
) {
}
