package com.worldmap.game.populationbattle.application;

import org.springframework.stereotype.Component;

@Component
public class PopulationBattleGameScoringPolicy {

	public PopulationBattleAnswerJudgement judge(
		Integer selectedOptionNumber,
		Integer correctOptionNumber,
		int stageNumber,
		int attemptNumber,
		int livesRemaining
	) {
		boolean correct = selectedOptionNumber.equals(correctOptionNumber);
		if (!correct) {
			return new PopulationBattleAnswerJudgement(false, 0);
		}

		int baseScore = 90 + ((stageNumber - 1) * 15);
		int lifeBonus = livesRemaining * 10;
		int attemptBonus = switch (attemptNumber) {
			case 1 -> 30;
			case 2 -> 10;
			default -> 0;
		};

		return new PopulationBattleAnswerJudgement(true, baseScore + lifeBonus + attemptBonus);
	}
}
