package com.worldmap.game.flag.web;

import com.worldmap.auth.application.CurrentMemberAccessService;
import com.worldmap.auth.application.GameSessionAccessContextResolver;
import com.worldmap.auth.application.GuestSessionKeyManager;
import com.worldmap.game.flag.application.FlagGameAnswerView;
import com.worldmap.game.flag.application.FlagGameService;
import com.worldmap.game.flag.application.FlagGameSessionResultView;
import com.worldmap.game.flag.application.FlagGameStartView;
import com.worldmap.game.flag.application.FlagGameStateView;
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
@RequestMapping("/api/games/flag/sessions")
public class FlagGameApiController {

	private final FlagGameService flagGameService;
	private final CurrentMemberAccessService currentMemberAccessService;
	private final GuestSessionKeyManager guestSessionKeyManager;
	private final GameSessionAccessContextResolver gameSessionAccessContextResolver;

	public FlagGameApiController(
		FlagGameService flagGameService,
		CurrentMemberAccessService currentMemberAccessService,
		GuestSessionKeyManager guestSessionKeyManager,
		GameSessionAccessContextResolver gameSessionAccessContextResolver
	) {
		this.flagGameService = flagGameService;
		this.currentMemberAccessService = currentMemberAccessService;
		this.guestSessionKeyManager = guestSessionKeyManager;
		this.gameSessionAccessContextResolver = gameSessionAccessContextResolver;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public FlagGameStartView start(@Valid @RequestBody StartFlagGameRequest request, HttpServletRequest httpRequest) {
		var currentMember = currentMemberAccessService.currentMember(httpRequest).orElse(null);
		if (currentMember != null) {
			return flagGameService.startMemberGame(currentMember.memberId(), currentMember.nickname());
		}

		return flagGameService.startGuestGame(
			request.nickname(),
			guestSessionKeyManager.ensureGuestSessionKey(httpRequest.getSession())
		);
	}

	@GetMapping("/{sessionId}/state")
	public FlagGameStateView currentState(@PathVariable UUID sessionId, HttpServletRequest request) {
		return flagGameService.getCurrentState(sessionId, gameSessionAccessContextResolver.resolve(request));
	}

	@GetMapping("/{sessionId}/round")
	public FlagGameStateView currentRoundAlias(@PathVariable UUID sessionId, HttpServletRequest request) {
		return flagGameService.getCurrentState(sessionId, gameSessionAccessContextResolver.resolve(request));
	}

	@PostMapping("/{sessionId}/restart")
	public FlagGameStartView restart(@PathVariable UUID sessionId, HttpServletRequest request) {
		return flagGameService.restartGame(sessionId, gameSessionAccessContextResolver.resolve(request));
	}

	@PostMapping("/{sessionId}/answer")
	public FlagGameAnswerView answer(
		@PathVariable UUID sessionId,
		@Valid @RequestBody SubmitFlagAnswerRequest request,
		HttpServletRequest httpRequest
	) {
		return flagGameService.submitAnswer(
			sessionId,
			request.stageNumber(),
			request.stageId(),
			request.expectedAttemptNumber(),
			request.selectedOptionNumber(),
			gameSessionAccessContextResolver.resolve(httpRequest)
		);
	}

	@GetMapping("/{sessionId}")
	public FlagGameSessionResultView sessionResult(@PathVariable UUID sessionId, HttpServletRequest request) {
		return flagGameService.getSessionResult(sessionId, gameSessionAccessContextResolver.resolve(request));
	}
}
