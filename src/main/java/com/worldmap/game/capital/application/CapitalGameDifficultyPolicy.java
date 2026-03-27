package com.worldmap.game.capital.application;

import org.springframework.stereotype.Component;

@Component
public class CapitalGameDifficultyPolicy {

	public CapitalGameDifficultyPlan resolve(int stageNumber, int totalCountryCount) {
		if (stageNumber < 1) {
			throw new IllegalArgumentException("stageNumber는 1 이상이어야 합니다.");
		}

		if (totalCountryCount < 4) {
			throw new IllegalArgumentException("totalCountryCount는 4 이상이어야 합니다.");
		}

		if (stageNumber <= 5) {
			return new CapitalGameDifficultyPlan("Band A · 주요 국가", Math.min(24, totalCountryCount));
		}

		if (stageNumber <= 12) {
			return new CapitalGameDifficultyPlan("Band B · 지역 확장", Math.min(60, totalCountryCount));
		}

		if (stageNumber <= 24) {
			return new CapitalGameDifficultyPlan("Band C · 글로벌", Math.min(110, totalCountryCount));
		}

		if (stageNumber <= 40) {
			return new CapitalGameDifficultyPlan("Band D · 고난도", Math.min(160, totalCountryCount));
		}

		return new CapitalGameDifficultyPlan("Band E · 전체 국가", totalCountryCount);
	}
}
