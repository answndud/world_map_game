package com.worldmap.game.population.application;

import org.springframework.stereotype.Component;

@Component
public class PopulationGamePrecisionScoringPolicy {

	public PopulationAnswerJudgement judge(
		Long submittedPopulation,
		Long targetPopulation,
		int stageNumber,
		int attemptNumber,
		int livesRemaining
	) {
		double errorRatePercent = calculateErrorRatePercent(submittedPopulation, targetPopulation);

		if (errorRatePercent > 20.0) {
			return new PopulationAnswerJudgement(false, 0, errorRatePercent);
		}

		int baseScore = baseScore(errorRatePercent, stageNumber);
		int lifeBonus = livesRemaining * 10;
		int attemptBonus = switch (attemptNumber) {
			case 1 -> 40;
			case 2 -> 20;
			default -> 0;
		};

		return new PopulationAnswerJudgement(true, baseScore + lifeBonus + attemptBonus, errorRatePercent);
	}

	private double calculateErrorRatePercent(Long submittedPopulation, Long targetPopulation) {
		if (submittedPopulation == null || submittedPopulation <= 0L) {
			throw new IllegalArgumentException("입력 인구수는 1 이상이어야 합니다.");
		}

		if (targetPopulation == null || targetPopulation <= 0L) {
			throw new IllegalArgumentException("정답 인구수는 1 이상이어야 합니다.");
		}

		return (Math.abs(submittedPopulation - targetPopulation) * 100.0) / targetPopulation;
	}

	private int baseScore(double errorRatePercent, int stageNumber) {
		if (errorRatePercent <= 5.0) {
			return 170 + ((stageNumber - 1) * 20);
		}
		if (errorRatePercent <= 12.0) {
			return 125 + ((stageNumber - 1) * 16);
		}
		return 90 + ((stageNumber - 1) * 12);
	}
}
