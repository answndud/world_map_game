package com.worldmap.game.population.web;

import com.worldmap.auth.application.GameSessionAccessContextResolver;
import com.worldmap.auth.application.MemberSessionManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import com.worldmap.game.population.application.PopulationGameService;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PopulationGamePageController {

	private final PopulationGameService populationGameService;
	private final MemberSessionManager memberSessionManager;
	private final GameSessionAccessContextResolver gameSessionAccessContextResolver;

	public PopulationGamePageController(
		PopulationGameService populationGameService,
		MemberSessionManager memberSessionManager,
		GameSessionAccessContextResolver gameSessionAccessContextResolver
	) {
		this.populationGameService = populationGameService;
		this.memberSessionManager = memberSessionManager;
		this.gameSessionAccessContextResolver = gameSessionAccessContextResolver;
	}

	@GetMapping("/games/population/start")
	public String startPage(HttpSession httpSession, Model model) {
		memberSessionManager.currentMember(httpSession)
			.ifPresent(currentMember -> model.addAttribute("authenticatedNickname", currentMember.nickname()));
		return "population-game/start";
	}

	@GetMapping("/games/population/play/{sessionId}")
	public String playPage(@PathVariable UUID sessionId, HttpServletRequest request, Model model) {
		populationGameService.assertSessionAccessible(sessionId, gameSessionAccessContextResolver.resolve(request));
		model.addAttribute("sessionId", sessionId);
		return "population-game/play";
	}

	@GetMapping("/games/population/result/{sessionId}")
	public String resultPage(@PathVariable UUID sessionId, HttpServletRequest request, Model model) {
		model.addAttribute("result", populationGameService.getSessionResult(sessionId, gameSessionAccessContextResolver.resolve(request)));
		return "population-game/result";
	}
}
