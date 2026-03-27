package com.worldmap.game.populationbattle.application;

import org.springframework.stereotype.Component;

@Component
public class PopulationBattleGameDifficultyPolicy {

	public PopulationBattleGameDifficultyPlan resolve(int stageNumber, int totalCountryCount) {
		if (stageNumber < 1) {
			throw new IllegalArgumentException("stageNumber는 1 이상이어야 합니다.");
		}
		if (totalCountryCount < 4) {
			throw new IllegalArgumentException("totalCountryCount는 4 이상이어야 합니다.");
		}

		if (stageNumber <= 5) {
			return new PopulationBattleGameDifficultyPlan("Band A · 큰 격차", Math.min(30, totalCountryCount), 12, 26);
		}
		if (stageNumber <= 12) {
			return new PopulationBattleGameDifficultyPlan("Band B · 근접 비교", Math.min(60, totalCountryCount), 7, 16);
		}
		if (stageNumber <= 24) {
			return new PopulationBattleGameDifficultyPlan("Band C · 글로벌", Math.min(110, totalCountryCount), 4, 10);
		}
		if (stageNumber <= 40) {
			return new PopulationBattleGameDifficultyPlan("Band D · 고난도", Math.min(160, totalCountryCount), 2, 6);
		}

		return new PopulationBattleGameDifficultyPlan("Band E · 초근접", totalCountryCount, 1, 4);
	}
}
