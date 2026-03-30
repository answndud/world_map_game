package com.worldmap.game.capital.web;

import com.worldmap.auth.application.GameSessionAccessContextResolver;
import com.worldmap.auth.application.MemberSessionManager;
import com.worldmap.game.capital.application.CapitalGameService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class CapitalGamePageController {

	private final CapitalGameService capitalGameService;
	private final MemberSessionManager memberSessionManager;
	private final GameSessionAccessContextResolver gameSessionAccessContextResolver;

	public CapitalGamePageController(
		CapitalGameService capitalGameService,
		MemberSessionManager memberSessionManager,
		GameSessionAccessContextResolver gameSessionAccessContextResolver
	) {
		this.capitalGameService = capitalGameService;
		this.memberSessionManager = memberSessionManager;
		this.gameSessionAccessContextResolver = gameSessionAccessContextResolver;
	}

	@GetMapping("/games/capital/start")
	public String startPage(HttpSession httpSession, Model model) {
		memberSessionManager.currentMember(httpSession)
			.ifPresent(currentMember -> model.addAttribute("authenticatedNickname", currentMember.nickname()));
		return "capital-game/start";
	}

	@GetMapping("/games/capital/play/{sessionId}")
	public String playPage(@PathVariable UUID sessionId, HttpServletRequest request, Model model) {
		capitalGameService.assertSessionAccessible(sessionId, gameSessionAccessContextResolver.resolve(request));
		model.addAttribute("sessionId", sessionId);
		return "capital-game/play";
	}

	@GetMapping("/games/capital/result/{sessionId}")
	public String resultPage(@PathVariable UUID sessionId, HttpServletRequest request, Model model) {
		model.addAttribute("result", capitalGameService.getSessionResult(sessionId, gameSessionAccessContextResolver.resolve(request)));
		return "capital-game/result";
	}
}
