package com.worldmap.game.location.application;

public record LocationAnswerJudgement(
	boolean correct,
	int awardedScore
) {
}
