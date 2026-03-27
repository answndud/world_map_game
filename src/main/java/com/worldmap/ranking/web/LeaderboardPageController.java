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
		model.addAttribute(
			"capitalAll",
			leaderboardService.getLeaderboard(LeaderboardGameMode.CAPITAL, LeaderboardScope.ALL, 10)
		);
		model.addAttribute(
			"flagAll",
			leaderboardService.getLeaderboard(LeaderboardGameMode.FLAG, LeaderboardScope.ALL, 10)
		);
		model.addAttribute(
			"populationBattleAll",
			leaderboardService.getLeaderboard(LeaderboardGameMode.POPULATION_BATTLE, LeaderboardScope.ALL, 10)
		);
		model.addAttribute(
			"locationDaily",
			leaderboardService.getLeaderboard(LeaderboardGameMode.LOCATION, LeaderboardScope.DAILY, 10)
		);
		model.addAttribute(
			"capitalDaily",
			leaderboardService.getLeaderboard(LeaderboardGameMode.CAPITAL, LeaderboardScope.DAILY, 10)
		);
		model.addAttribute(
			"flagDaily",
			leaderboardService.getLeaderboard(LeaderboardGameMode.FLAG, LeaderboardScope.DAILY, 10)
		);
		model.addAttribute(
			"populationBattleDaily",
			leaderboardService.getLeaderboard(LeaderboardGameMode.POPULATION_BATTLE, LeaderboardScope.DAILY, 10)
		);
		model.addAttribute(
			"populationAll",
			leaderboardService.getLeaderboard(LeaderboardGameMode.POPULATION, LeaderboardScope.ALL, 10)
		);
		model.addAttribute(
			"populationDaily",
			leaderboardService.getLeaderboard(LeaderboardGameMode.POPULATION, LeaderboardScope.DAILY, 10)
		);
		return "ranking/index";
	}
}
