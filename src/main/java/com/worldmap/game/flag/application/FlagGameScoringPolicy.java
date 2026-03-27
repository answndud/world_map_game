package com.worldmap.game.flag.application;

import org.springframework.stereotype.Component;

@Component
public class FlagGameScoringPolicy {

	public FlagAnswerJudgement judge(
		Integer selectedOptionNumber,
		Integer correctOptionNumber,
		int stageNumber,
		int attemptNumber,
		int livesRemaining
	) {
		boolean correct = selectedOptionNumber.equals(correctOptionNumber);
		if (!correct) {
			return new FlagAnswerJudgement(false, 0);
		}

		int baseScore = 90 + ((stageNumber - 1) * 15);
		int lifeBonus = livesRemaining * 10;
		int attemptBonus = switch (attemptNumber) {
			case 1 -> 30;
			case 2 -> 10;
			default -> 0;
		};

		return new FlagAnswerJudgement(true, baseScore + lifeBonus + attemptBonus);
	}
}
