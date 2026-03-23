package com.worldmap.ranking.domain;

public enum LeaderboardScope {

	ALL("all"),
	DAILY("daily");

	private final String queryValue;

	LeaderboardScope(String queryValue) {
		this.queryValue = queryValue;
	}

	public String getQueryValue() {
		return queryValue;
	}

	public static LeaderboardScope from(String rawValue) {
		for (LeaderboardScope value : values()) {
			if (value.queryValue.equalsIgnoreCase(rawValue) || value.name().equalsIgnoreCase(rawValue)) {
				return value;
			}
		}

		throw new IllegalArgumentException("지원하지 않는 랭킹 범위입니다: " + rawValue);
	}
}
