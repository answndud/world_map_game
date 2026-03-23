package com.worldmap.game.population.web;

import com.worldmap.game.population.application.PopulationGameAnswerView;
import com.worldmap.game.population.application.PopulationGameCurrentRoundView;
import com.worldmap.game.population.application.PopulationGameService;
import com.worldmap.game.population.application.PopulationGameSessionResultView;
import com.worldmap.game.population.application.PopulationGameStartView;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/games/population/sessions")
public class PopulationGameApiController {

	private final PopulationGameService populationGameService;

	public PopulationGameApiController(PopulationGameService populationGameService) {
		this.populationGameService = populationGameService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public PopulationGameStartView start(@Valid @RequestBody StartPopulationGameRequest request) {
		return populationGameService.startGame(request.nickname());
	}

	@GetMapping("/{sessionId}/round")
	public PopulationGameCurrentRoundView currentRound(@PathVariable UUID sessionId) {
		return populationGameService.getCurrentRound(sessionId);
	}

	@PostMapping("/{sessionId}/answer")
	public PopulationGameAnswerView answer(
		@PathVariable UUID sessionId,
		@Valid @RequestBody SubmitPopulationAnswerRequest request
	) {
		return populationGameService.submitAnswer(
			sessionId,
			request.roundNumber(),
			request.selectedOptionNumber()
		);
	}

	@GetMapping("/{sessionId}")
	public PopulationGameSessionResultView sessionResult(@PathVariable UUID sessionId) {
		return populationGameService.getSessionResult(sessionId);
	}
}
