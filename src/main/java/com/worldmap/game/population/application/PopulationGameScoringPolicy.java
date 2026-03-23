package com.worldmap.game.population.application;

import org.springframework.stereotype.Component;

@Component
public class PopulationGameScoringPolicy {

	public PopulationAnswerJudgement judge(Integer selectedOptionNumber, Integer correctOptionNumber) {
		boolean correct = selectedOptionNumber.equals(correctOptionNumber);
		return new PopulationAnswerJudgement(correct, correct ? 100 : 0);
	}
}
