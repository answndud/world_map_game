package com.worldmap.ranking.application;

import org.springframework.stereotype.Component;

@Component
public class LeaderboardRankingPolicy {

	private static final long SCORE_WEIGHT = 1_000_000L;
	private static final long CLEAR_WEIGHT = 1_000L;
	private static final int ATTEMPT_BONUS_CAP = 999;

	public long rankingScore(Integer totalScore, Integer clearedStageCount, Integer totalAttemptCount) {
		if (totalScore == null || totalScore < 0) {
			throw new IllegalArgumentException("totalScore는 0 이상이어야 합니다.");
		}

		if (clearedStageCount == null || clearedStageCount < 0) {
			throw new IllegalArgumentException("clearedStageCount는 0 이상이어야 합니다.");
		}

		if (totalAttemptCount == null || totalAttemptCount < 0) {
			throw new IllegalArgumentException("totalAttemptCount는 0 이상이어야 합니다.");
		}

		long attemptBonus = Math.max(0, ATTEMPT_BONUS_CAP - Math.min(totalAttemptCount, ATTEMPT_BONUS_CAP));
		return (long) totalScore * SCORE_WEIGHT
			+ (long) clearedStageCount * CLEAR_WEIGHT
			+ attemptBonus;
	}
}
