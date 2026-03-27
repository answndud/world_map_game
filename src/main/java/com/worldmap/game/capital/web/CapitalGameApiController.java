package com.worldmap.game.capital.web;

import com.worldmap.auth.application.AuthenticatedMemberSession;
import com.worldmap.auth.application.GuestSessionKeyManager;
import com.worldmap.auth.application.MemberSessionManager;
import com.worldmap.game.capital.application.CapitalGameAnswerView;
import com.worldmap.game.capital.application.CapitalGameService;
import com.worldmap.game.capital.application.CapitalGameSessionResultView;
import com.worldmap.game.capital.application.CapitalGameStartView;
import com.worldmap.game.capital.application.CapitalGameStateView;
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
@RequestMapping("/api/games/capital/sessions")
public class CapitalGameApiController {

	private final CapitalGameService capitalGameService;
	private final GuestSessionKeyManager guestSessionKeyManager;
	private final MemberSessionManager memberSessionManager;

	public CapitalGameApiController(
		CapitalGameService capitalGameService,
		GuestSessionKeyManager guestSessionKeyManager,
		MemberSessionManager memberSessionManager
	) {
		this.capitalGameService = capitalGameService;
		this.guestSessionKeyManager = guestSessionKeyManager;
		this.memberSessionManager = memberSessionManager;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public CapitalGameStartView start(@Valid @RequestBody StartCapitalGameRequest request, HttpSession httpSession) {
		AuthenticatedMemberSession currentMember = memberSessionManager.currentMember(httpSession).orElse(null);
		if (currentMember != null) {
			return capitalGameService.startMemberGame(currentMember.memberId(), currentMember.nickname());
		}

		return capitalGameService.startGuestGame(
			request.nickname(),
			guestSessionKeyManager.ensureGuestSessionKey(httpSession)
		);
	}

	@GetMapping("/{sessionId}/state")
	public CapitalGameStateView currentState(@PathVariable UUID sessionId) {
		return capitalGameService.getCurrentState(sessionId);
	}

	@GetMapping("/{sessionId}/round")
	public CapitalGameStateView currentRoundAlias(@PathVariable UUID sessionId) {
		return capitalGameService.getCurrentState(sessionId);
	}

	@PostMapping("/{sessionId}/restart")
	public CapitalGameStartView restart(@PathVariable UUID sessionId) {
		return capitalGameService.restartGame(sessionId);
	}

	@PostMapping("/{sessionId}/answer")
	public CapitalGameAnswerView answer(
		@PathVariable UUID sessionId,
		@Valid @RequestBody SubmitCapitalAnswerRequest request
	) {
		return capitalGameService.submitAnswer(
			sessionId,
			request.stageNumber(),
			request.selectedOptionNumber()
		);
	}

	@GetMapping("/{sessionId}")
	public CapitalGameSessionResultView sessionResult(@PathVariable UUID sessionId) {
		return capitalGameService.getSessionResult(sessionId);
	}
}
