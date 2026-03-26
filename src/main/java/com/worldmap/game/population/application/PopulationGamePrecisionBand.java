package com.worldmap.game.population.application;

public enum PopulationGamePrecisionBand {

	PRECISE_HIT("정밀 적중", "오차율 5% 이하"),
	CLOSE_HIT("근접 적중", "오차율 12% 이하"),
	SAFE_HIT("허용 범위 정답", "오차율 20% 이하"),
	MISS("오답", "오차율 20% 초과");

	private final String label;
	private final String guide;

	PopulationGamePrecisionBand(String label, String guide) {
		this.label = label;
		this.guide = guide;
	}

	public String label() {
		return label;
	}

	public String guide() {
		return guide;
	}
}
