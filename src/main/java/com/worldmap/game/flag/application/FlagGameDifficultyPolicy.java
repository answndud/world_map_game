package com.worldmap.game.flag.application;

import org.springframework.stereotype.Component;

@Component
public class FlagGameDifficultyPolicy {

	public FlagGameDifficultyPlan resolve(int stageNumber, int totalCountryCount) {
		if (stageNumber < 1) {
			throw new IllegalArgumentException("stageNumber는 1 이상이어야 합니다.");
		}

		if (totalCountryCount < 4) {
			throw new IllegalArgumentException("totalCountryCount는 4 이상이어야 합니다.");
		}

		if (stageNumber <= 4) {
			return new FlagGameDifficultyPlan("Pool A · 대표 국기", Math.min(6, totalCountryCount));
		}

		if (stageNumber <= 8) {
			return new FlagGameDifficultyPlan("Pool B · 자산 확장", Math.min(9, totalCountryCount));
		}

		return new FlagGameDifficultyPlan("Pool C · 전체 국기", totalCountryCount);
	}
}
