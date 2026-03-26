package com.worldmap.game.location.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.worldmap.game.location.domain.LocationGameLevel;
import org.junit.jupiter.api.Test;

class LocationGameScoringPolicyTest {

	private final LocationGameScoringPolicy policy = new LocationGameScoringPolicy();

	@Test
	void exactCountrySelectionGetsStageAndLifeBonus() {
		LocationAnswerJudgement judgement = policy.judge(LocationGameLevel.LEVEL_1, "KOR", "KOR", 3, 1, 3);

		assertThat(judgement.awardedScore()).isEqualTo(200);
		assertThat(judgement.correct()).isTrue();
		assertThat(judgement.hintPenalty()).isZero();
	}

	@Test
	void wrongCountrySelectionGetsZeroScore() {
		LocationAnswerJudgement judgement = policy.judge(LocationGameLevel.LEVEL_1, "JPN", "KOR", 2, 2, 2);

		assertThat(judgement.awardedScore()).isEqualTo(0);
		assertThat(judgement.correct()).isFalse();
		assertThat(judgement.hintPenalty()).isZero();
	}

	@Test
	void levelTwoCorrectAnswerAppliesHintDebtByAttemptCount() {
		LocationAnswerJudgement judgement = policy.judge(LocationGameLevel.LEVEL_2, "KOR", "KOR", 1, 2, 2);

		assertThat(judgement.correct()).isTrue();
		assertThat(judgement.hintPenalty()).isEqualTo(15);
		assertThat(judgement.awardedScore()).isEqualTo(115);
	}
}
