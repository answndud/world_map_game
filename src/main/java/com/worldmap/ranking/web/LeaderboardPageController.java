package com.worldmap.ranking.web;

import com.worldmap.ranking.application.LeaderboardService;
import com.worldmap.ranking.domain.LeaderboardGameMode;
import com.worldmap.ranking.domain.LeaderboardScope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LeaderboardPageController {

	private final LeaderboardService leaderboardService;

	public LeaderboardPageController(LeaderboardService leaderboardService) {
		this.leaderboardService = leaderboardService;
	}

	@GetMapping("/ranking")
	public String rankingPage(Model model) {
		model.addAttribute(
			"locationAll",
			leaderboardService.getLeaderboard(LeaderboardGameMode.LOCATION, LeaderboardScope.ALL, 10)
		);
		return "ranking/index";
	}
}
