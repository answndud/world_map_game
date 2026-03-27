package com.worldmap.game.capital.web;

import com.worldmap.auth.application.MemberSessionManager;
import com.worldmap.game.capital.application.CapitalGameService;
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

	public CapitalGamePageController(
		CapitalGameService capitalGameService,
		MemberSessionManager memberSessionManager
	) {
		this.capitalGameService = capitalGameService;
		this.memberSessionManager = memberSessionManager;
	}

	@GetMapping("/games/capital/start")
	public String startPage(HttpSession httpSession, Model model) {
		memberSessionManager.currentMember(httpSession)
			.ifPresent(currentMember -> model.addAttribute("authenticatedNickname", currentMember.nickname()));
		return "capital-game/start";
	}

	@GetMapping("/games/capital/play/{sessionId}")
	public String playPage(@PathVariable UUID sessionId, Model model) {
		model.addAttribute("sessionId", sessionId);
		return "capital-game/play";
	}

	@GetMapping("/games/capital/result/{sessionId}")
	public String resultPage(@PathVariable UUID sessionId, Model model) {
		model.addAttribute("result", capitalGameService.getSessionResult(sessionId));
		return "capital-game/result";
	}
}
