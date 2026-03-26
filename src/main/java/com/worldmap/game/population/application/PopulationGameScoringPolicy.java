package com.worldmap.game.population.application;

import org.springframework.stereotype.Component;

@Component
public class PopulationGameScoringPolicy {

	public PopulationAnswerJudgement judge(
		Integer selectedOptionNumber,
		Integer correctOptionNumber,
		int stageNumber,
		int attemptNumber,
		int livesRemaining
	) {
		boolean correct = selectedOptionNumber.equals(correctOptionNumber);
		if (!correct) {
			return new PopulationAnswerJudgement(false, 0, null, null);
		}

		int baseScore = 90 + ((stageNumber - 1) * 15);
		int lifeBonus = livesRemaining * 10;
		int attemptBonus = switch (attemptNumber) {
			case 1 -> 30;
			case 2 -> 10;
			default -> 0;
		};

		return new PopulationAnswerJudgement(true, baseScore + lifeBonus + attemptBonus, null, null);
	}
}
