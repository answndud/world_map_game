package com.worldmap.game.flag.web;

import com.worldmap.auth.application.MemberSessionManager;
import com.worldmap.game.flag.application.FlagGameService;
import jakarta.servlet.http.HttpSession;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class FlagGamePageController {

	private final FlagGameService flagGameService;
	private final MemberSessionManager memberSessionManager;

	public FlagGamePageController(
		FlagGameService flagGameService,
		MemberSessionManager memberSessionManager
	) {
		this.flagGameService = flagGameService;
		this.memberSessionManager = memberSessionManager;
	}

	@GetMapping("/games/flag/start")
	public String startPage(HttpSession httpSession, Model model) {
		memberSessionManager.currentMember(httpSession)
			.ifPresent(currentMember -> model.addAttribute("authenticatedNickname", currentMember.nickname()));
		return "flag-game/start";
	}

	@GetMapping("/games/flag/play/{sessionId}")
	public String playPage(@PathVariable UUID sessionId, Model model) {
		model.addAttribute("sessionId", sessionId);
		return "flag-game/play";
	}

	@GetMapping("/games/flag/result/{sessionId}")
	public String resultPage(@PathVariable UUID sessionId, Model model) {
		model.addAttribute("result", flagGameService.getSessionResult(sessionId));
		return "flag-game/result";
	}
}
