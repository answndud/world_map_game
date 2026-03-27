package com.worldmap.ranking.domain;

public enum LeaderboardGameMode {

	LOCATION("location"),
	CAPITAL("capital"),
	POPULATION_BATTLE("population-battle"),
	POPULATION("population");

	private final String pathValue;

	LeaderboardGameMode(String pathValue) {
		this.pathValue = pathValue;
	}

	public String getPathValue() {
		return pathValue;
	}

	public static LeaderboardGameMode from(String rawValue) {
		for (LeaderboardGameMode value : values()) {
			if (value.pathValue.equalsIgnoreCase(rawValue) || value.name().equalsIgnoreCase(rawValue)) {
				return value;
			}
		}

		throw new IllegalArgumentException("지원하지 않는 랭킹 모드입니다: " + rawValue);
	}
}
