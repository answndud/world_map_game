package com.worldmap.game.location.web;

import com.worldmap.auth.application.GameSessionAccessContextResolver;
import jakarta.servlet.http.HttpServletRequest;
import com.worldmap.game.location.application.LocationGameService;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class LocationGamePageController {

	private final LocationGameService locationGameService;
	private final GameSessionAccessContextResolver gameSessionAccessContextResolver;

	public LocationGamePageController(
		LocationGameService locationGameService,
		GameSessionAccessContextResolver gameSessionAccessContextResolver
	) {
		this.locationGameService = locationGameService;
		this.gameSessionAccessContextResolver = gameSessionAccessContextResolver;
	}

	@GetMapping("/games/location/start")
	public String startPage() {
		return "location-game/start";
	}

	@GetMapping("/games/location/play/{sessionId}")
	public String playPage(@PathVariable UUID sessionId, HttpServletRequest request, Model model) {
		locationGameService.assertSessionAccessible(sessionId, gameSessionAccessContextResolver.resolve(request));
		model.addAttribute("sessionId", sessionId);
		return "location-game/play";
	}

	@GetMapping("/games/location/result/{sessionId}")
	public String resultPage(@PathVariable UUID sessionId, HttpServletRequest request, Model model) {
		model.addAttribute("result", locationGameService.getSessionResult(sessionId, gameSessionAccessContextResolver.resolve(request)));
		return "location-game/result";
	}
}
