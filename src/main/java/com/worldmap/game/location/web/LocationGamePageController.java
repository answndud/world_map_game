package com.worldmap.game.location.web;

import com.worldmap.game.location.application.LocationGameService;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class LocationGamePageController {

	private final LocationGameService locationGameService;

	public LocationGamePageController(LocationGameService locationGameService) {
		this.locationGameService = locationGameService;
	}

	@GetMapping("/games/location/start")
	public String startPage() {
		return "location-game/start";
	}

	@GetMapping("/games/location/play/{sessionId}")
	public String playPage(@PathVariable UUID sessionId, Model model) {
		model.addAttribute("sessionId", sessionId);
		return "location-game/play";
	}

	@GetMapping("/games/location/result/{sessionId}")
	public String resultPage(@PathVariable UUID sessionId, Model model) {
		model.addAttribute("result", locationGameService.getSessionResult(sessionId));
		return "location-game/result";
	}
}
