package com.worldmap.game.location.application;

import com.worldmap.game.location.domain.LocationGameLevel;
import org.springframework.stereotype.Component;

@Component
public class LocationGameDifficultyPolicy {

	public LocationGameDifficultyPlan resolve(int stageNumber, int totalCountryCount) {
		return resolve(LocationGameLevel.LEVEL_1, stageNumber, totalCountryCount);
	}

	public LocationGameDifficultyPlan resolve(
		LocationGameLevel gameLevel,
		int stageNumber,
		int totalCountryCount
	) {
		if (stageNumber < 1) {
			throw new IllegalArgumentException("stageNumber는 1 이상이어야 합니다.");
		}

		if (totalCountryCount < 1) {
			throw new IllegalArgumentException("totalCountryCount는 1 이상이어야 합니다.");
		}

		if (gameLevel == LocationGameLevel.LEVEL_2) {
			return resolveLevelTwo(stageNumber, totalCountryCount);
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

	private LocationGameDifficultyPlan resolveLevelTwo(int stageNumber, int totalCountryCount) {
		if (stageNumber <= 4) {
			return new LocationGameDifficultyPlan("Vector A · 거리 힌트", Math.min(48, totalCountryCount));
		}
		if (stageNumber <= 10) {
			return new LocationGameDifficultyPlan("Vector B · 지역 혼동", Math.min(96, totalCountryCount));
		}
		if (stageNumber <= 20) {
			return new LocationGameDifficultyPlan("Vector C · 글로벌 확장", Math.min(150, totalCountryCount));
		}
		if (stageNumber <= 35) {
			return new LocationGameDifficultyPlan("Vector D · 전대륙", Math.min(190, totalCountryCount));
		}
		return new LocationGameDifficultyPlan("Vector E · 전체 국가", totalCountryCount);
	}
}
