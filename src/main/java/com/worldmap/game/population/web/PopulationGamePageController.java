package com.worldmap.game.population.web;

import com.worldmap.auth.application.GameSessionAccessContextResolver;
import jakarta.servlet.http.HttpServletRequest;
import com.worldmap.game.population.application.PopulationGameService;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PopulationGamePageController {

	private final PopulationGameService populationGameService;
	private final GameSessionAccessContextResolver gameSessionAccessContextResolver;

	public PopulationGamePageController(
		PopulationGameService populationGameService,
		GameSessionAccessContextResolver gameSessionAccessContextResolver
	) {
		this.populationGameService = populationGameService;
		this.gameSessionAccessContextResolver = gameSessionAccessContextResolver;
	}

	@GetMapping("/games/population/start")
	public String startPage() {
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
