package com.worldmap.game.location.application;

import org.springframework.stereotype.Component;

@Component
public class LocationGameDifficultyPolicy {

	public LocationGameDifficultyPlan resolve(int stageNumber, int totalCountryCount) {
		if (stageNumber < 1) {
			throw new IllegalArgumentException("stageNumber는 1 이상이어야 합니다.");
		}

		if (totalCountryCount < 1) {
			throw new IllegalArgumentException("totalCountryCount는 1 이상이어야 합니다.");
		}

		if (stageNumber <= 5) {
			return new LocationGameDifficultyPlan("Sector A · 주요 국가", Math.min(28, totalCountryCount));
		}

		if (stageNumber <= 12) {
			return new LocationGameDifficultyPlan("Sector B · 지역 확장", Math.min(60, totalCountryCount));
		}

		if (stageNumber <= 25) {
			return new LocationGameDifficultyPlan("Sector C · 글로벌", Math.min(110, totalCountryCount));
		}

		if (stageNumber <= 45) {
			return new LocationGameDifficultyPlan("Sector D · 고난도", Math.min(160, totalCountryCount));
		}

		return new LocationGameDifficultyPlan("Sector E · 전체 국가", totalCountryCount);
	}
}
