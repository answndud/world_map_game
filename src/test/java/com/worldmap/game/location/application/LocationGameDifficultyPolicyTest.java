package com.worldmap.game.location.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LocationGameDifficultyPolicyTest {

	private final LocationGameDifficultyPolicy locationGameDifficultyPolicy = new LocationGameDifficultyPolicy();

	@Test
	void candidatePoolExpandsAsStageAdvances() {
		LocationGameDifficultyPlan earlyPlan = locationGameDifficultyPolicy.resolve(1, 194);
		LocationGameDifficultyPlan midPlan = locationGameDifficultyPolicy.resolve(18, 194);
		LocationGameDifficultyPlan latePlan = locationGameDifficultyPolicy.resolve(60, 194);

		assertThat(earlyPlan.candidatePoolSize()).isLessThan(midPlan.candidatePoolSize());
		assertThat(midPlan.candidatePoolSize()).isLessThan(latePlan.candidatePoolSize());
		assertThat(latePlan.candidatePoolSize()).isEqualTo(194);
	}
}
