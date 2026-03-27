package com.worldmap.game.flag.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FlagGameDifficultyPolicyTest {

	private final FlagGameDifficultyPolicy difficultyPolicy = new FlagGameDifficultyPolicy();

	@Test
	void earlyMidAndLateRoundsExposePlayerFacingDifficultyPhases() {
		FlagGameDifficultyPlan early = difficultyPolicy.resolve(1, 36);
		FlagGameDifficultyPlan mid = difficultyPolicy.resolve(5, 36);
		FlagGameDifficultyPlan late = difficultyPolicy.resolve(9, 36);

		assertThat(early.label()).isEqualTo("기본 라운드");
		assertThat(early.guide()).contains("같은 대륙");
		assertThat(early.candidatePoolSize()).isEqualTo(6);
		assertThat(early.stableContinentTargetsOnly()).isTrue();

		assertThat(mid.label()).isEqualTo("확장 라운드");
		assertThat(mid.guide()).contains("지역 fallback");
		assertThat(mid.candidatePoolSize()).isEqualTo(12);
		assertThat(mid.stableContinentTargetsOnly()).isFalse();

		assertThat(late.label()).isEqualTo("전체 라운드");
		assertThat(late.guide()).contains("36개 전체 국기 pool");
		assertThat(late.candidatePoolSize()).isEqualTo(36);
		assertThat(late.stableContinentTargetsOnly()).isFalse();
	}
}
