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
			return new FlagGameDifficultyPlan(
				"기본 라운드",
				"같은 대륙 비교가 쉬운 대표 국기부터 시작합니다.",
				Math.min(6, totalCountryCount),
				true
			);
		}

		if (stageNumber <= 8) {
			return new FlagGameDifficultyPlan(
				"확장 라운드",
				"자산이 넓어지고 지역 fallback이 섞이기 시작합니다.",
				Math.min(12, totalCountryCount),
				false
			);
		}

		return new FlagGameDifficultyPlan(
			"전체 라운드",
			"36개 전체 국기 pool에서 나라가 빠르게 섞입니다.",
			totalCountryCount,
			false
		);
	}
}
