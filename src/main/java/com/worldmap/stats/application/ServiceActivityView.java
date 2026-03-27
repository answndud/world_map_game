package com.worldmap.stats.application;

public record ServiceActivityView(
	long totalMemberCount,
	long todayActiveMemberCount,
	long todayActiveGuestCount,
	long todayStartedSessionCount,
	long todayCompletedRunCount,
	long todayLocationCompletedRunCount,
	long todayCapitalCompletedRunCount,
	long todayFlagCompletedRunCount,
	long todayPopulationBattleCompletedRunCount,
	long todayPopulationCompletedRunCount
) {

	public long todayActivePlayerCount() {
		return todayActiveMemberCount + todayActiveGuestCount;
	}
}
