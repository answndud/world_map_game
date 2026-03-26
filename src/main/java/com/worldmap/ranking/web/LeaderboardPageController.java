package com.worldmap.ranking.web;

import com.worldmap.ranking.application.LeaderboardService;
import com.worldmap.ranking.domain.LeaderboardGameLevel;
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
			leaderboardService.getLeaderboard(LeaderboardGameMode.LOCATION, LeaderboardGameLevel.LEVEL_1, LeaderboardScope.ALL, 10)
		);
		model.addAttribute(
			"locationDaily",
			leaderboardService.getLeaderboard(LeaderboardGameMode.LOCATION, LeaderboardGameLevel.LEVEL_1, LeaderboardScope.DAILY, 10)
		);
		model.addAttribute(
			"locationLevel2All",
			leaderboardService.getLeaderboard(LeaderboardGameMode.LOCATION, LeaderboardGameLevel.LEVEL_2, LeaderboardScope.ALL, 10)
		);
		model.addAttribute(
			"locationLevel2Daily",
			leaderboardService.getLeaderboard(LeaderboardGameMode.LOCATION, LeaderboardGameLevel.LEVEL_2, LeaderboardScope.DAILY, 10)
		);
		model.addAttribute(
			"populationAll",
			leaderboardService.getLeaderboard(LeaderboardGameMode.POPULATION, LeaderboardGameLevel.LEVEL_1, LeaderboardScope.ALL, 10)
		);
		model.addAttribute(
			"populationDaily",
			leaderboardService.getLeaderboard(LeaderboardGameMode.POPULATION, LeaderboardGameLevel.LEVEL_1, LeaderboardScope.DAILY, 10)
		);
		model.addAttribute(
			"populationLevel2All",
			leaderboardService.getLeaderboard(LeaderboardGameMode.POPULATION, LeaderboardGameLevel.LEVEL_2, LeaderboardScope.ALL, 10)
		);
		model.addAttribute(
			"populationLevel2Daily",
			leaderboardService.getLeaderboard(LeaderboardGameMode.POPULATION, LeaderboardGameLevel.LEVEL_2, LeaderboardScope.DAILY, 10)
		);
		return "ranking/index";
	}
}
