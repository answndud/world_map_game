package com.worldmap.game.populationbattle.web;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SubmitPopulationBattleAnswerRequest(
	@NotNull(message = "stageNumber는 필수입니다.")
	Integer stageNumber,
	Long stageId,
	Integer expectedAttemptNumber,
	@NotNull(message = "selectedOptionNumber는 필수입니다.")
	@Min(value = 1, message = "selectedOptionNumber는 1 이상이어야 합니다.")
	Integer selectedOptionNumber
) {
}
