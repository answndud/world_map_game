package com.worldmap.game.location.web;

import com.worldmap.auth.application.CurrentMemberAccessService;
import com.worldmap.auth.application.GameSessionAccessContextResolver;
import com.worldmap.auth.application.GuestSessionKeyManager;
import com.worldmap.game.location.application.LocationGameAnswerView;
import com.worldmap.game.location.application.LocationGameService;
import com.worldmap.game.location.application.LocationGameSessionResultView;
import com.worldmap.game.location.application.LocationGameStartView;
import com.worldmap.game.location.application.LocationGameStateView;
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
@RequestMapping("/api/games/location/sessions")
public class LocationGameApiController {

	private final LocationGameService locationGameService;
	private final CurrentMemberAccessService currentMemberAccessService;
	private final GuestSessionKeyManager guestSessionKeyManager;
	private final GameSessionAccessContextResolver gameSessionAccessContextResolver;

	public LocationGameApiController(
		LocationGameService locationGameService,
		CurrentMemberAccessService currentMemberAccessService,
		GuestSessionKeyManager guestSessionKeyManager,
		GameSessionAccessContextResolver gameSessionAccessContextResolver
	) {
		this.locationGameService = locationGameService;
		this.currentMemberAccessService = currentMemberAccessService;
		this.guestSessionKeyManager = guestSessionKeyManager;
		this.gameSessionAccessContextResolver = gameSessionAccessContextResolver;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public LocationGameStartView start(@Valid @RequestBody StartLocationGameRequest request, HttpSession httpSession) {
		var currentMember = currentMemberAccessService.currentMember(httpSession).orElse(null);
		if (currentMember != null) {
			return locationGameService.startMemberGame(currentMember.memberId(), currentMember.nickname());
		}

		return locationGameService.startGuestGame(
			request.nickname(),
			guestSessionKeyManager.ensureGuestSessionKey(httpSession)
		);
	}

	@GetMapping("/{sessionId}/state")
	public LocationGameStateView currentState(@PathVariable UUID sessionId, HttpServletRequest request) {
		return locationGameService.getCurrentState(sessionId, gameSessionAccessContextResolver.resolve(request));
	}

	@GetMapping("/{sessionId}/round")
	public LocationGameStateView currentRoundAlias(@PathVariable UUID sessionId, HttpServletRequest request) {
		return locationGameService.getCurrentState(sessionId, gameSessionAccessContextResolver.resolve(request));
	}

	@PostMapping("/{sessionId}/restart")
	public LocationGameStartView restart(@PathVariable UUID sessionId, HttpServletRequest request) {
		return locationGameService.restartGame(sessionId, gameSessionAccessContextResolver.resolve(request));
	}

	@PostMapping("/{sessionId}/answer")
	public LocationGameAnswerView answer(
		@PathVariable UUID sessionId,
		@Valid @RequestBody SubmitLocationAnswerRequest request,
		HttpServletRequest httpRequest
	) {
		return locationGameService.submitAnswer(
			sessionId,
			request.stageNumber(),
			request.stageId(),
			request.expectedAttemptNumber(),
			request.selectedCountryIso3Code(),
			gameSessionAccessContextResolver.resolve(httpRequest)
		);
	}

	@GetMapping("/{sessionId}/result")
	public LocationGameSessionResultView sessionResult(@PathVariable UUID sessionId, HttpServletRequest request) {
		return locationGameService.getSessionResult(sessionId, gameSessionAccessContextResolver.resolve(request));
	}

	@GetMapping("/{sessionId}")
	public LocationGameSessionResultView sessionResultAlias(@PathVariable UUID sessionId, HttpServletRequest request) {
		return locationGameService.getSessionResult(sessionId, gameSessionAccessContextResolver.resolve(request));
	}
}
