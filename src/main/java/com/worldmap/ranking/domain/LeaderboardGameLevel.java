package com.worldmap.ranking.domain;

public enum LeaderboardGameLevel {

	LEVEL_1("l1"),
	LEVEL_2("l2");

	private final String redisToken;

	LeaderboardGameLevel(String redisToken) {
		this.redisToken = redisToken;
	}

	public String getRedisToken() {
		return redisToken;
	}

	public static LeaderboardGameLevel from(String rawValue) {
		for (LeaderboardGameLevel value : values()) {
			if (value.redisToken.equalsIgnoreCase(rawValue) || value.name().equalsIgnoreCase(rawValue)) {
				return value;
			}
		}

		throw new IllegalArgumentException("지원하지 않는 랭킹 레벨입니다: " + rawValue);
	}
}
