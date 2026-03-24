package com.worldmap.game.location.web;

import com.worldmap.auth.application.MemberSessionManager;
import jakarta.servlet.http.HttpSession;
import com.worldmap.game.location.application.LocationGameService;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class LocationGamePageController {

	private final LocationGameService locationGameService;
	private final MemberSessionManager memberSessionManager;

	public LocationGamePageController(LocationGameService locationGameService, MemberSessionManager memberSessionManager) {
		this.locationGameService = locationGameService;
		this.memberSessionManager = memberSessionManager;
	}

	@GetMapping("/games/location/start")
	public String startPage(HttpSession httpSession, Model model) {
		memberSessionManager.currentMember(httpSession)
			.ifPresent(currentMember -> model.addAttribute("authenticatedNickname", currentMember.nickname()));
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
