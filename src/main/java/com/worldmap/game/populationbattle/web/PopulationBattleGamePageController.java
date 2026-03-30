package com.worldmap.game.populationbattle.web;

import com.worldmap.auth.application.GameSessionAccessContextResolver;
import com.worldmap.auth.application.MemberSessionManager;
import com.worldmap.game.populationbattle.application.PopulationBattleGameService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PopulationBattleGamePageController {

	private final PopulationBattleGameService populationBattleGameService;
	private final MemberSessionManager memberSessionManager;
	private final GameSessionAccessContextResolver gameSessionAccessContextResolver;

	public PopulationBattleGamePageController(
		PopulationBattleGameService populationBattleGameService,
		MemberSessionManager memberSessionManager,
		GameSessionAccessContextResolver gameSessionAccessContextResolver
	) {
		this.populationBattleGameService = populationBattleGameService;
		this.memberSessionManager = memberSessionManager;
		this.gameSessionAccessContextResolver = gameSessionAccessContextResolver;
	}

	@GetMapping("/games/population-battle/start")
	public String startPage(HttpSession httpSession, Model model) {
		memberSessionManager.currentMember(httpSession)
			.ifPresent(currentMember -> model.addAttribute("authenticatedNickname", currentMember.nickname()));
		return "population-battle-game/start";
	}

	@GetMapping("/games/population-battle/play/{sessionId}")
	public String playPage(@PathVariable UUID sessionId, HttpServletRequest request, Model model) {
		populationBattleGameService.assertSessionAccessible(sessionId, gameSessionAccessContextResolver.resolve(request));
		model.addAttribute("sessionId", sessionId);
		return "population-battle-game/play";
	}

	@GetMapping("/games/population-battle/result/{sessionId}")
	public String resultPage(@PathVariable UUID sessionId, HttpServletRequest request, Model model) {
		model.addAttribute("result", populationBattleGameService.getSessionResult(sessionId, gameSessionAccessContextResolver.resolve(request)));
		return "population-battle-game/result";
	}
}
