package com.worldmap.game.location.application;

import com.worldmap.game.location.domain.LocationGameLevel;
import org.springframework.stereotype.Component;

@Component
public class LocationGameScoringPolicy {

	private static final int LEVEL_TWO_HINT_PENALTY_PER_USE = 15;

	public LocationAnswerJudgement judge(
		LocationGameLevel gameLevel,
		String selectedCountryIso3Code,
		String targetCountryIso3Code,
		int stageNumber,
		int attemptNumber,
		int livesRemaining
	) {
		boolean correct = targetCountryIso3Code.equalsIgnoreCase(selectedCountryIso3Code);
		if (!correct) {
			return new LocationAnswerJudgement(false, 0, 0);
		}

		int baseScore = 100 + ((stageNumber - 1) * 20);
		int lifeBonus = livesRemaining * 10;
		int attemptBonus = switch (attemptNumber) {
			case 1 -> 30;
			case 2 -> 10;
			default -> 0;
		};
		int hintPenalty = hintPenaltyFor(gameLevel, Math.max(0, attemptNumber - 1));

		return new LocationAnswerJudgement(true, Math.max(0, baseScore + lifeBonus + attemptBonus - hintPenalty), hintPenalty);
	}

	public int hintPenaltyFor(LocationGameLevel gameLevel, int hintUseCount) {
		if (!gameLevel.usesDistanceHint()) {
			return 0;
		}

		return Math.max(0, hintUseCount) * LEVEL_TWO_HINT_PENALTY_PER_USE;
	}
}
