package com.worldmap.game.populationbattle.web;

import com.worldmap.auth.application.AuthenticatedMemberSession;
import com.worldmap.auth.application.GameSessionAccessContextResolver;
import com.worldmap.auth.application.GuestSessionKeyManager;
import com.worldmap.auth.application.MemberSessionManager;
import com.worldmap.game.populationbattle.application.PopulationBattleGameAnswerView;
import com.worldmap.game.populationbattle.application.PopulationBattleGameService;
import com.worldmap.game.populationbattle.application.PopulationBattleGameSessionResultView;
import com.worldmap.game.populationbattle.application.PopulationBattleGameStartView;
import com.worldmap.game.populationbattle.application.PopulationBattleGameStateView;
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
@RequestMapping("/api/games/population-battle/sessions")
public class PopulationBattleGameApiController {

	private final PopulationBattleGameService populationBattleGameService;
	private final GuestSessionKeyManager guestSessionKeyManager;
	private final MemberSessionManager memberSessionManager;
	private final GameSessionAccessContextResolver gameSessionAccessContextResolver;

	public PopulationBattleGameApiController(
		PopulationBattleGameService populationBattleGameService,
		GuestSessionKeyManager guestSessionKeyManager,
		MemberSessionManager memberSessionManager,
		GameSessionAccessContextResolver gameSessionAccessContextResolver
	) {
		this.populationBattleGameService = populationBattleGameService;
		this.guestSessionKeyManager = guestSessionKeyManager;
		this.memberSessionManager = memberSessionManager;
		this.gameSessionAccessContextResolver = gameSessionAccessContextResolver;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public PopulationBattleGameStartView start(
		@Valid @RequestBody StartPopulationBattleGameRequest request,
		HttpSession httpSession
	) {
		AuthenticatedMemberSession currentMember = memberSessionManager.currentMember(httpSession).orElse(null);
		if (currentMember != null) {
			return populationBattleGameService.startMemberGame(currentMember.memberId(), currentMember.nickname());
		}

		return populationBattleGameService.startGuestGame(
			request.nickname(),
			guestSessionKeyManager.ensureGuestSessionKey(httpSession)
		);
	}

	@GetMapping("/{sessionId}/state")
	public PopulationBattleGameStateView currentState(@PathVariable UUID sessionId, HttpServletRequest request) {
		return populationBattleGameService.getCurrentState(sessionId, gameSessionAccessContextResolver.resolve(request));
	}

	@GetMapping("/{sessionId}/round")
	public PopulationBattleGameStateView currentRoundAlias(@PathVariable UUID sessionId, HttpServletRequest request) {
		return populationBattleGameService.getCurrentState(sessionId, gameSessionAccessContextResolver.resolve(request));
	}

	@PostMapping("/{sessionId}/restart")
	public PopulationBattleGameStartView restart(@PathVariable UUID sessionId, HttpServletRequest request) {
		return populationBattleGameService.restartGame(sessionId, gameSessionAccessContextResolver.resolve(request));
	}

	@PostMapping("/{sessionId}/answer")
	public PopulationBattleGameAnswerView answer(
		@PathVariable UUID sessionId,
		@Valid @RequestBody SubmitPopulationBattleAnswerRequest request,
		HttpServletRequest httpRequest
	) {
		return populationBattleGameService.submitAnswer(
			sessionId,
			request.stageNumber(),
			request.stageId(),
			request.expectedAttemptNumber(),
			request.selectedOptionNumber(),
			gameSessionAccessContextResolver.resolve(httpRequest)
		);
	}

	@GetMapping("/{sessionId}")
	public PopulationBattleGameSessionResultView sessionResult(@PathVariable UUID sessionId, HttpServletRequest request) {
		return populationBattleGameService.getSessionResult(sessionId, gameSessionAccessContextResolver.resolve(request));
	}
}
