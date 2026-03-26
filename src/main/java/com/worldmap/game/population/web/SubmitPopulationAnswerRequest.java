package com.worldmap.game.population.web;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SubmitPopulationAnswerRequest(
	@NotNull(message = "stageNumber는 필수입니다.")
	Integer stageNumber,
	@Min(value = 1, message = "selectedOptionNumber는 1 이상이어야 합니다.")
	Integer selectedOptionNumber,
	@Min(value = 1, message = "submittedPopulation은 1 이상이어야 합니다.")
	Long submittedPopulation
) {
}
