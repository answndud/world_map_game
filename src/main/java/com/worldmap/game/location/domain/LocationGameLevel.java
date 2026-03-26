package com.worldmap.game.location.domain;

public enum LocationGameLevel {

	LEVEL_1("Level 1", "기본 탐색", false),
	LEVEL_2("Level 2", "거리 힌트", true);

	private final String displayLabel;
	private final String hintModeLabel;
	private final boolean usesDistanceHint;

	LocationGameLevel(String displayLabel, String hintModeLabel, boolean usesDistanceHint) {
		this.displayLabel = displayLabel;
		this.hintModeLabel = hintModeLabel;
		this.usesDistanceHint = usesDistanceHint;
	}

	public static LocationGameLevel from(String rawValue) {
		if (rawValue == null || rawValue.isBlank()) {
			return LEVEL_1;
		}

		for (LocationGameLevel gameLevel : values()) {
			if (gameLevel.name().equalsIgnoreCase(rawValue.trim())) {
				return gameLevel;
			}
		}

		throw new IllegalArgumentException("지원하지 않는 위치 게임 레벨입니다: " + rawValue);
	}

	public String displayLabel() {
		return displayLabel;
	}

	public String hintModeLabel() {
		return hintModeLabel;
	}

	public boolean usesDistanceHint() {
		return usesDistanceHint;
	}
}
