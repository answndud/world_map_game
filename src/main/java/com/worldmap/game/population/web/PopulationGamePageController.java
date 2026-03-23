package com.worldmap.game.population.web;

import com.worldmap.game.population.application.PopulationGameService;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PopulationGamePageController {

	private final PopulationGameService populationGameService;

	public PopulationGamePageController(PopulationGameService populationGameService) {
		this.populationGameService = populationGameService;
	}

	@GetMapping("/games/population/start")
	public String startPage() {
		return "population-game/start";
	}

	@GetMapping("/games/population/play/{sessionId}")
	public String playPage(@PathVariable UUID sessionId, Model model) {
		model.addAttribute("sessionId", sessionId);
		return "population-game/play";
	}

	@GetMapping("/games/population/result/{sessionId}")
	public String resultPage(@PathVariable UUID sessionId, Model model) {
		model.addAttribute("result", populationGameService.getSessionResult(sessionId));
		return "population-game/result";
	}
}
