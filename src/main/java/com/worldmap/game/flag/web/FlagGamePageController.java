package com.worldmap.game.flag.web;

import com.worldmap.auth.application.GameSessionAccessContextResolver;
import com.worldmap.game.flag.application.FlagGameService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class FlagGamePageController {

	private final FlagGameService flagGameService;
	private final GameSessionAccessContextResolver gameSessionAccessContextResolver;

	public FlagGamePageController(
		FlagGameService flagGameService,
		GameSessionAccessContextResolver gameSessionAccessContextResolver
	) {
		this.flagGameService = flagGameService;
		this.gameSessionAccessContextResolver = gameSessionAccessContextResolver;
	}

	@GetMapping("/games/flag/start")
	public String startPage() {
		return "flag-game/start";
	}

	@GetMapping("/games/flag/play/{sessionId}")
	public String playPage(@PathVariable UUID sessionId, HttpServletRequest request, Model model) {
		flagGameService.assertSessionAccessible(sessionId, gameSessionAccessContextResolver.resolve(request));
		model.addAttribute("sessionId", sessionId);
		return "flag-game/play";
	}

	@GetMapping("/games/flag/result/{sessionId}")
	public String resultPage(@PathVariable UUID sessionId, HttpServletRequest request, Model model) {
		model.addAttribute("result", flagGameService.getSessionResult(sessionId, gameSessionAccessContextResolver.resolve(request)));
		return "flag-game/result";
	}
}
