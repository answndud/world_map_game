package com.worldmap.game.population.web;

import com.worldmap.auth.application.CurrentMemberAccessService;
import com.worldmap.auth.application.GameSessionAccessContextResolver;
import com.worldmap.auth.application.GuestSessionKeyManager;
import com.worldmap.game.population.application.PopulationGameAnswerView;
import com.worldmap.game.population.application.PopulationGameService;
import com.worldmap.game.population.application.PopulationGameSessionResultView;
import com.worldmap.game.population.application.PopulationGameStartView;
import com.worldmap.game.population.application.PopulationGameStateView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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
	private final CurrentMemberAccessService currentMemberAccessService;
	private final GuestSessionKeyManager guestSessionKeyManager;
	private final GameSessionAccessContextResolver gameSessionAccessContextResolver;

	public PopulationGameApiController(
		PopulationGameService populationGameService,
		CurrentMemberAccessService currentMemberAccessService,
		GuestSessionKeyManager guestSessionKeyManager,
		GameSessionAccessContextResolver gameSessionAccessContextResolver
	) {
		this.populationGameService = populationGameService;
		this.currentMemberAccessService = currentMemberAccessService;
		this.guestSessionKeyManager = guestSessionKeyManager;
		this.gameSessionAccessContextResolver = gameSessionAccessContextResolver;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public PopulationGameStartView start(@Valid @RequestBody StartPopulationGameRequest request, HttpSession httpSession) {
		var currentMember = currentMemberAccessService.currentMember(httpSession).orElse(null);
		if (currentMember != null) {
			return populationGameService.startMemberGame(currentMember.memberId(), currentMember.nickname());
		}

		return populationGameService.startGuestGame(
			request.nickname(),
			guestSessionKeyManager.ensureGuestSessionKey(httpSession)
		);
	}

	@GetMapping("/{sessionId}/state")
	public PopulationGameStateView currentState(@PathVariable UUID sessionId, HttpServletRequest request) {
		return populationGameService.getCurrentState(sessionId, gameSessionAccessContextResolver.resolve(request));
	}

	@GetMapping("/{sessionId}/round")
	public PopulationGameStateView currentRoundAlias(@PathVariable UUID sessionId, HttpServletRequest request) {
		return populationGameService.getCurrentState(sessionId, gameSessionAccessContextResolver.resolve(request));
	}

	@PostMapping("/{sessionId}/restart")
	public PopulationGameStartView restart(@PathVariable UUID sessionId, HttpServletRequest request) {
		return populationGameService.restartGame(sessionId, gameSessionAccessContextResolver.resolve(request));
	}

	@PostMapping("/{sessionId}/answer")
	public PopulationGameAnswerView answer(
		@PathVariable UUID sessionId,
		@Valid @RequestBody SubmitPopulationAnswerRequest request,
		HttpServletRequest httpRequest
	) {
		return populationGameService.submitAnswer(
			sessionId,
			request.stageNumber(),
			request.stageId(),
			request.expectedAttemptNumber(),
			request.selectedOptionNumber(),
			gameSessionAccessContextResolver.resolve(httpRequest)
		);
	}

	@GetMapping("/{sessionId}")
	public PopulationGameSessionResultView sessionResult(@PathVariable UUID sessionId, HttpServletRequest request) {
		return populationGameService.getSessionResult(sessionId, gameSessionAccessContextResolver.resolve(request));
	}
}
