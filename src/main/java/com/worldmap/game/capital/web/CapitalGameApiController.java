package com.worldmap.game.capital.web;

import com.worldmap.auth.application.CurrentMemberAccessService;
import com.worldmap.auth.application.GameSessionAccessContextResolver;
import com.worldmap.auth.application.GuestSessionKeyManager;
import com.worldmap.game.capital.application.CapitalGameAnswerView;
import com.worldmap.game.capital.application.CapitalGameService;
import com.worldmap.game.capital.application.CapitalGameSessionResultView;
import com.worldmap.game.capital.application.CapitalGameStartView;
import com.worldmap.game.capital.application.CapitalGameStateView;
import jakarta.servlet.http.HttpServletRequest;
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
@RequestMapping("/api/games/capital/sessions")
public class CapitalGameApiController {

	private final CapitalGameService capitalGameService;
	private final CurrentMemberAccessService currentMemberAccessService;
	private final GuestSessionKeyManager guestSessionKeyManager;
	private final GameSessionAccessContextResolver gameSessionAccessContextResolver;

	public CapitalGameApiController(
		CapitalGameService capitalGameService,
		CurrentMemberAccessService currentMemberAccessService,
		GuestSessionKeyManager guestSessionKeyManager,
		GameSessionAccessContextResolver gameSessionAccessContextResolver
	) {
		this.capitalGameService = capitalGameService;
		this.currentMemberAccessService = currentMemberAccessService;
		this.guestSessionKeyManager = guestSessionKeyManager;
		this.gameSessionAccessContextResolver = gameSessionAccessContextResolver;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public CapitalGameStartView start(@Valid @RequestBody StartCapitalGameRequest request, HttpServletRequest httpRequest) {
		var currentMember = currentMemberAccessService.currentMember(httpRequest).orElse(null);
		if (currentMember != null) {
			return capitalGameService.startMemberGame(currentMember.memberId(), currentMember.nickname());
		}

		return capitalGameService.startGuestGame(
			request.nickname(),
			guestSessionKeyManager.ensureGuestSessionKey(httpRequest.getSession())
		);
	}

	@GetMapping("/{sessionId}/state")
	public CapitalGameStateView currentState(@PathVariable UUID sessionId, HttpServletRequest request) {
		return capitalGameService.getCurrentState(sessionId, gameSessionAccessContextResolver.resolve(request));
	}

	@GetMapping("/{sessionId}/round")
	public CapitalGameStateView currentRoundAlias(@PathVariable UUID sessionId, HttpServletRequest request) {
		return capitalGameService.getCurrentState(sessionId, gameSessionAccessContextResolver.resolve(request));
	}

	@PostMapping("/{sessionId}/restart")
	public CapitalGameStartView restart(@PathVariable UUID sessionId, HttpServletRequest request) {
		return capitalGameService.restartGame(sessionId, gameSessionAccessContextResolver.resolve(request));
	}

	@PostMapping("/{sessionId}/answer")
	public CapitalGameAnswerView answer(
		@PathVariable UUID sessionId,
		@Valid @RequestBody SubmitCapitalAnswerRequest request,
		HttpServletRequest httpRequest
	) {
		return capitalGameService.submitAnswer(
			sessionId,
			request.stageNumber(),
			request.stageId(),
			request.expectedAttemptNumber(),
			request.selectedOptionNumber(),
			gameSessionAccessContextResolver.resolve(httpRequest)
		);
	}

	@GetMapping("/{sessionId}")
	public CapitalGameSessionResultView sessionResult(@PathVariable UUID sessionId, HttpServletRequest request) {
		return capitalGameService.getSessionResult(sessionId, gameSessionAccessContextResolver.resolve(request));
	}
}
