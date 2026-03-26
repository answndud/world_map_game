package com.worldmap.game.population.web;

import com.worldmap.auth.application.AuthenticatedMemberSession;
import com.worldmap.auth.application.GuestSessionKeyManager;
import com.worldmap.auth.application.MemberSessionManager;
import com.worldmap.game.population.application.PopulationGameAnswerView;
import com.worldmap.game.population.application.PopulationGameService;
import com.worldmap.game.population.application.PopulationGameSessionResultView;
import com.worldmap.game.population.application.PopulationGameStartView;
import com.worldmap.game.population.application.PopulationGameStateView;
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
	private final GuestSessionKeyManager guestSessionKeyManager;
	private final MemberSessionManager memberSessionManager;

	public PopulationGameApiController(
		PopulationGameService populationGameService,
		GuestSessionKeyManager guestSessionKeyManager,
		MemberSessionManager memberSessionManager
	) {
		this.populationGameService = populationGameService;
		this.guestSessionKeyManager = guestSessionKeyManager;
		this.memberSessionManager = memberSessionManager;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public PopulationGameStartView start(@Valid @RequestBody StartPopulationGameRequest request, HttpSession httpSession) {
		AuthenticatedMemberSession currentMember = memberSessionManager.currentMember(httpSession).orElse(null);
		if (currentMember != null) {
			return populationGameService.startMemberGame(currentMember.memberId(), currentMember.nickname());
		}

		return populationGameService.startGuestGame(
			request.nickname(),
			guestSessionKeyManager.ensureGuestSessionKey(httpSession)
		);
	}

	@GetMapping("/{sessionId}/state")
	public PopulationGameStateView currentState(@PathVariable UUID sessionId) {
		return populationGameService.getCurrentState(sessionId);
	}

	@GetMapping("/{sessionId}/round")
	public PopulationGameStateView currentRoundAlias(@PathVariable UUID sessionId) {
		return populationGameService.getCurrentState(sessionId);
	}

	@PostMapping("/{sessionId}/restart")
	public PopulationGameStartView restart(@PathVariable UUID sessionId) {
		return populationGameService.restartGame(sessionId);
	}

	@PostMapping("/{sessionId}/answer")
	public PopulationGameAnswerView answer(
		@PathVariable UUID sessionId,
		@Valid @RequestBody SubmitPopulationAnswerRequest request
	) {
		return populationGameService.submitAnswer(
			sessionId,
			request.stageNumber(),
			request.selectedOptionNumber()
		);
	}

	@GetMapping("/{sessionId}")
	public PopulationGameSessionResultView sessionResult(@PathVariable UUID sessionId) {
		return populationGameService.getSessionResult(sessionId);
	}
}
