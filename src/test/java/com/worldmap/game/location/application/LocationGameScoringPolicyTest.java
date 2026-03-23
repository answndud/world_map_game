package com.worldmap.game.location.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LocationGameScoringPolicyTest {

	private final LocationGameScoringPolicy policy = new LocationGameScoringPolicy();

	@Test
	void exactCountrySelectionGetsStageAndLifeBonus() {
		LocationAnswerJudgement judgement = policy.judge("KOR", "KOR", 3, 1, 3);

		assertThat(judgement.awardedScore()).isEqualTo(200);
		assertThat(judgement.correct()).isTrue();
	}

	@Test
	void wrongCountrySelectionGetsZeroScore() {
		LocationAnswerJudgement judgement = policy.judge("JPN", "KOR", 2, 2, 2);

		assertThat(judgement.awardedScore()).isEqualTo(0);
		assertThat(judgement.correct()).isFalse();
	}
}
