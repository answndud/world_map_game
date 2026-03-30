package com.worldmap.game.location.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SubmitLocationAnswerRequest(
	@NotNull(message = "stageNumber는 필수입니다.")
	Integer stageNumber,
	Long stageId,
	Integer expectedAttemptNumber,
	@NotBlank(message = "selectedCountryIso3Code는 필수입니다.")
	@Size(min = 3, max = 3, message = "selectedCountryIso3Code는 ISO3 코드여야 합니다.")
	String selectedCountryIso3Code
) {
}
