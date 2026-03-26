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
		PopulationGamePrecisionBand precisionBand = resolveBand(errorRatePercent);

		if (precisionBand == PopulationGamePrecisionBand.MISS) {
			return new PopulationAnswerJudgement(false, 0, errorRatePercent, precisionBand);
		}

		int baseScore = baseScore(errorRatePercent, stageNumber);
		int lifeBonus = livesRemaining * 10;
		int attemptBonus = switch (attemptNumber) {
			case 1 -> 40;
			case 2 -> 20;
			default -> 0;
		};

		return new PopulationAnswerJudgement(true, baseScore + lifeBonus + attemptBonus, errorRatePercent, precisionBand);
	}

	public PopulationGamePrecisionBand resolveBand(Long submittedPopulation, Long targetPopulation) {
		return resolveBand(calculateErrorRatePercent(submittedPopulation, targetPopulation));
	}

	public PopulationGamePrecisionBand resolveBand(double errorRatePercent) {
		if (errorRatePercent <= 5.0) {
			return PopulationGamePrecisionBand.PRECISE_HIT;
		}
		if (errorRatePercent <= 12.0) {
			return PopulationGamePrecisionBand.CLOSE_HIT;
		}
		if (errorRatePercent <= 20.0) {
			return PopulationGamePrecisionBand.SAFE_HIT;
		}
		return PopulationGamePrecisionBand.MISS;
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
		PopulationGamePrecisionBand precisionBand = resolveBand(errorRatePercent);
		if (precisionBand == PopulationGamePrecisionBand.PRECISE_HIT) {
			return 170 + ((stageNumber - 1) * 20);
		}
		if (precisionBand == PopulationGamePrecisionBand.CLOSE_HIT) {
			return 125 + ((stageNumber - 1) * 16);
		}
		return 90 + ((stageNumber - 1) * 12);
	}
}
