package com.worldmap.game.flag.web;

import com.worldmap.auth.application.AuthenticatedMemberSession;
import com.worldmap.auth.application.GuestSessionKeyManager;
import com.worldmap.auth.application.MemberSessionManager;
import com.worldmap.game.flag.application.FlagGameAnswerView;
import com.worldmap.game.flag.application.FlagGameService;
import com.worldmap.game.flag.application.FlagGameSessionResultView;
import com.worldmap.game.flag.application.FlagGameStartView;
import com.worldmap.game.flag.application.FlagGameStateView;
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
@RequestMapping("/api/games/flag/sessions")
public class FlagGameApiController {

	private final FlagGameService flagGameService;
	private final GuestSessionKeyManager guestSessionKeyManager;
	private final MemberSessionManager memberSessionManager;

	public FlagGameApiController(
		FlagGameService flagGameService,
		GuestSessionKeyManager guestSessionKeyManager,
		MemberSessionManager memberSessionManager
	) {
		this.flagGameService = flagGameService;
		this.guestSessionKeyManager = guestSessionKeyManager;
		this.memberSessionManager = memberSessionManager;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public FlagGameStartView start(@Valid @RequestBody StartFlagGameRequest request, HttpSession httpSession) {
		AuthenticatedMemberSession currentMember = memberSessionManager.currentMember(httpSession).orElse(null);
		if (currentMember != null) {
			return flagGameService.startMemberGame(currentMember.memberId(), currentMember.nickname());
		}

		return flagGameService.startGuestGame(
			request.nickname(),
			guestSessionKeyManager.ensureGuestSessionKey(httpSession)
		);
	}

	@GetMapping("/{sessionId}/state")
	public FlagGameStateView currentState(@PathVariable UUID sessionId) {
		return flagGameService.getCurrentState(sessionId);
	}

	@GetMapping("/{sessionId}/round")
	public FlagGameStateView currentRoundAlias(@PathVariable UUID sessionId) {
		return flagGameService.getCurrentState(sessionId);
	}

	@PostMapping("/{sessionId}/restart")
	public FlagGameStartView restart(@PathVariable UUID sessionId) {
		return flagGameService.restartGame(sessionId);
	}

	@PostMapping("/{sessionId}/answer")
	public FlagGameAnswerView answer(
		@PathVariable UUID sessionId,
		@Valid @RequestBody SubmitFlagAnswerRequest request
	) {
		return flagGameService.submitAnswer(
			sessionId,
			request.stageNumber(),
			request.selectedOptionNumber()
		);
	}

	@GetMapping("/{sessionId}")
	public FlagGameSessionResultView sessionResult(@PathVariable UUID sessionId) {
		return flagGameService.getSessionResult(sessionId);
	}
}
