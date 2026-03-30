package com.worldmap.game.populationbattle.web;

import com.worldmap.auth.application.GameSessionAccessContextResolver;
import com.worldmap.game.populationbattle.application.PopulationBattleGameService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PopulationBattleGamePageController {

	private final PopulationBattleGameService populationBattleGameService;
	private final GameSessionAccessContextResolver gameSessionAccessContextResolver;

	public PopulationBattleGamePageController(
		PopulationBattleGameService populationBattleGameService,
		GameSessionAccessContextResolver gameSessionAccessContextResolver
	) {
		this.populationBattleGameService = populationBattleGameService;
		this.gameSessionAccessContextResolver = gameSessionAccessContextResolver;
	}

	@GetMapping("/games/population-battle/start")
	public String startPage() {
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
