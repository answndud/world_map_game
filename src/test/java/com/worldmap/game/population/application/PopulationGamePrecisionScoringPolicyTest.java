package com.worldmap.game.population.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PopulationGamePrecisionScoringPolicyTest {

	private final PopulationGamePrecisionScoringPolicy scoringPolicy = new PopulationGamePrecisionScoringPolicy();

	@Test
	void closeAnswerWithinFivePercentGetsHighestBandScore() {
		PopulationAnswerJudgement judgement = scoringPolicy.judge(95_000_000L, 100_000_000L, 3, 1, 3);

		assertThat(judgement.correct()).isTrue();
		assertThat(judgement.errorRatePercent()).isEqualTo(5.0);
		assertThat(judgement.precisionBand()).isEqualTo(PopulationGamePrecisionBand.PRECISE_HIT);
		assertThat(judgement.awardedScore()).isGreaterThan(200);
	}

	@Test
	void farAnswerBeyondTwentyPercentIsWrong() {
		PopulationAnswerJudgement judgement = scoringPolicy.judge(50_000_000L, 100_000_000L, 2, 1, 3);

		assertThat(judgement.correct()).isFalse();
		assertThat(judgement.awardedScore()).isZero();
		assertThat(judgement.errorRatePercent()).isEqualTo(50.0);
		assertThat(judgement.precisionBand()).isEqualTo(PopulationGamePrecisionBand.MISS);
	}
}
