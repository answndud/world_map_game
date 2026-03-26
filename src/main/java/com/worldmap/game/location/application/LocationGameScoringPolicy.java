package com.worldmap.game.location.application;

import org.springframework.stereotype.Component;

@Component
public class LocationGameScoringPolicy {

	public LocationAnswerJudgement judge(
		String selectedCountryIso3Code,
		String targetCountryIso3Code,
		int stageNumber,
		int attemptNumber,
		int livesRemaining
	) {
		boolean correct = targetCountryIso3Code.equalsIgnoreCase(selectedCountryIso3Code);
		if (!correct) {
			return new LocationAnswerJudgement(false, 0);
		}

		int baseScore = 100 + ((stageNumber - 1) * 20);
		int lifeBonus = livesRemaining * 10;
		int attemptBonus = switch (attemptNumber) {
			case 1 -> 30;
			case 2 -> 10;
			default -> 0;
		};

		return new LocationAnswerJudgement(true, baseScore + lifeBonus + attemptBonus);
	}
}
