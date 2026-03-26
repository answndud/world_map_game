package com.worldmap.ranking.web;

import com.worldmap.ranking.application.LeaderboardService;
import com.worldmap.ranking.application.LeaderboardView;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rankings")
public class LeaderboardApiController {

	private final LeaderboardService leaderboardService;

	public LeaderboardApiController(LeaderboardService leaderboardService) {
		this.leaderboardService = leaderboardService;
	}

	@GetMapping("/{gameMode}")
	public LeaderboardView leaderboard(
		@PathVariable String gameMode,
		@RequestParam(defaultValue = "ALL") String scope,
		@RequestParam(defaultValue = "10") Integer limit
	) {
		return leaderboardService.getLeaderboard(gameMode, scope, limit);
	}
}
