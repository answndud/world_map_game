package com.worldmap.stats.web;

import com.worldmap.ranking.application.LeaderboardService;
import com.worldmap.ranking.domain.LeaderboardGameLevel;
import com.worldmap.ranking.domain.LeaderboardGameMode;
import com.worldmap.ranking.domain.LeaderboardScope;
import com.worldmap.stats.application.ServiceActivityService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StatsPageController {

	private final ServiceActivityService serviceActivityService;
	private final LeaderboardService leaderboardService;

	public StatsPageController(
		ServiceActivityService serviceActivityService,
		LeaderboardService leaderboardService
	) {
		this.serviceActivityService = serviceActivityService;
		this.leaderboardService = leaderboardService;
	}

	@GetMapping("/stats")
	public String stats(Model model) {
		model.addAttribute("activity", serviceActivityService.loadTodayActivity());
		model.addAttribute("locationDailyTop", leaderboardService.getLeaderboard(
			LeaderboardGameMode.LOCATION,
			LeaderboardScope.DAILY,
			3
		));
		model.addAttribute("populationDailyTop", leaderboardService.getLeaderboard(
			LeaderboardGameMode.POPULATION,
			LeaderboardScope.DAILY,
			3
		));
		model.addAttribute("locationLevel2Highlight", leaderboardService.getLeaderboard(
			LeaderboardGameMode.LOCATION,
			LeaderboardGameLevel.LEVEL_2,
			LeaderboardScope.ALL,
			1
		));
		model.addAttribute("populationLevel2Highlight", leaderboardService.getLeaderboard(
			LeaderboardGameMode.POPULATION,
			LeaderboardGameLevel.LEVEL_2,
			LeaderboardScope.ALL,
			1
		));
		return "stats/index";
	}
}
