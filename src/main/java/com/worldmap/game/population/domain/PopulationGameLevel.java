package com.worldmap.game.population.domain;

public enum PopulationGameLevel {

	LEVEL_1("Level 1", "구간 선택", false),
	LEVEL_2("Level 2", "직접 입력", true);

	private final String displayLabel;
	private final String answerModeLabel;
	private final boolean exactInput;

	PopulationGameLevel(String displayLabel, String answerModeLabel, boolean exactInput) {
		this.displayLabel = displayLabel;
		this.answerModeLabel = answerModeLabel;
		this.exactInput = exactInput;
	}

	public static PopulationGameLevel from(String rawValue) {
		if (rawValue == null || rawValue.isBlank()) {
			return LEVEL_1;
		}

		for (PopulationGameLevel gameLevel : values()) {
			if (gameLevel.name().equalsIgnoreCase(rawValue.trim())) {
				return gameLevel;
			}
		}

		throw new IllegalArgumentException("지원하지 않는 인구수 게임 레벨입니다: " + rawValue);
	}

	public String displayLabel() {
		return displayLabel;
	}

	public String answerModeLabel() {
		return answerModeLabel;
	}

	public boolean usesExactInput() {
		return exactInput;
	}
}
