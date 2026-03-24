package com.worldmap.game.location.web;

import com.worldmap.auth.application.GuestSessionKeyManager;
import com.worldmap.game.location.application.LocationGameAnswerView;
import com.worldmap.game.location.application.LocationGameService;
import com.worldmap.game.location.application.LocationGameSessionResultView;
import com.worldmap.game.location.application.LocationGameStartView;
import com.worldmap.game.location.application.LocationGameStateView;
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
	private final GuestSessionKeyManager guestSessionKeyManager;

	public LocationGameApiController(
		LocationGameService locationGameService,
		GuestSessionKeyManager guestSessionKeyManager
	) {
		this.locationGameService = locationGameService;
		this.guestSessionKeyManager = guestSessionKeyManager;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public LocationGameStartView start(@Valid @RequestBody StartLocationGameRequest request, HttpSession httpSession) {
		return locationGameService.startGame(
			request.nickname(),
			guestSessionKeyManager.ensureGuestSessionKey(httpSession)
		);
	}

	@GetMapping("/{sessionId}/state")
	public LocationGameStateView currentState(@PathVariable UUID sessionId) {
		return locationGameService.getCurrentState(sessionId);
	}

	@GetMapping("/{sessionId}/round")
	public LocationGameStateView currentRoundAlias(@PathVariable UUID sessionId) {
		return locationGameService.getCurrentState(sessionId);
	}

	@PostMapping("/{sessionId}/restart")
	public LocationGameStartView restart(@PathVariable UUID sessionId) {
		return locationGameService.restartGame(sessionId);
	}

	@PostMapping("/{sessionId}/answer")
	public LocationGameAnswerView answer(
		@PathVariable UUID sessionId,
		@Valid @RequestBody SubmitLocationAnswerRequest request
	) {
		return locationGameService.submitAnswer(
			sessionId,
			request.stageNumber(),
			request.selectedCountryIso3Code()
		);
	}

	@GetMapping("/{sessionId}/result")
	public LocationGameSessionResultView sessionResult(@PathVariable UUID sessionId) {
		return locationGameService.getSessionResult(sessionId);
	}

	@GetMapping("/{sessionId}")
	public LocationGameSessionResultView sessionResultAlias(@PathVariable UUID sessionId) {
		return locationGameService.getSessionResult(sessionId);
	}
}
