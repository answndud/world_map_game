package com.worldmap.ranking.domain;

public enum LeaderboardGameLevel {

	LEVEL_1("l1");

	private final String redisToken;

	LeaderboardGameLevel(String redisToken) {
		this.redisToken = redisToken;
	}

	public String getRedisToken() {
		return redisToken;
	}
}
