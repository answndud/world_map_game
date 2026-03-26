package com.worldmap.ranking.application;

import com.worldmap.ranking.domain.LeaderboardGameMode;
import com.worldmap.ranking.domain.LeaderboardScope;
import java.time.LocalDate;
import java.util.List;

public record LeaderboardView(
	LeaderboardGameMode gameMode,
	LeaderboardScope scope,
	LocalDate targetDate,
	List<LeaderboardEntryView> entries
) {
}
