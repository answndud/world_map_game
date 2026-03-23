package com.worldmap.game.population.application;

import org.springframework.stereotype.Component;

@Component
public class PopulationGameDifficultyPolicy {

	public PopulationGameDifficultyPlan resolve(int stageNumber, int totalCountryCount) {
		if (stageNumber < 1) {
			throw new IllegalArgumentException("stageNumber는 1 이상이어야 합니다.");
		}

		if (totalCountryCount < 4) {
			throw new IllegalArgumentException("totalCountryCount는 4 이상이어야 합니다.");
		}

		if (stageNumber <= 5) {
			return new PopulationGameDifficultyPlan("Band A · 초대형 국가", Math.min(24, totalCountryCount));
		}

		if (stageNumber <= 12) {
			return new PopulationGameDifficultyPlan("Band B · 지역 확장", Math.min(60, totalCountryCount));
		}

		if (stageNumber <= 24) {
			return new PopulationGameDifficultyPlan("Band C · 글로벌", Math.min(110, totalCountryCount));
		}

		if (stageNumber <= 40) {
			return new PopulationGameDifficultyPlan("Band D · 고난도", Math.min(160, totalCountryCount));
		}

		return new PopulationGameDifficultyPlan("Band E · 전체 국가", totalCountryCount);
	}
}
