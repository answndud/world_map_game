package com.worldmap.game.population.domain;

import com.worldmap.game.common.domain.BaseGameSession;
import com.worldmap.game.common.domain.GameSessionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "population_game_session")
public class PopulationGameSession extends BaseGameSession {

	private static final int DEFAULT_LIVES = 3;

	@Column(name = "lives_remaining")
	private Integer livesRemaining;

	@Enumerated(EnumType.STRING)
	@Column(name = "game_level", length = 20)
	private PopulationGameLevel gameLevel;

	protected PopulationGameSession() {
	}

	private PopulationGameSession(
		UUID id,
		String playerNickname,
		Long memberId,
		String guestSessionKey,
		PopulationGameLevel gameLevel,
		Integer totalRounds
	) {
		super(id, playerNickname, memberId, guestSessionKey, totalRounds);
		this.livesRemaining = DEFAULT_LIVES;
		this.gameLevel = gameLevel;
	}

	public static PopulationGameSession ready(String playerNickname, PopulationGameLevel gameLevel, Integer totalRounds) {
		return new PopulationGameSession(UUID.randomUUID(), playerNickname, null, null, gameLevel, totalRounds);
	}

	public static PopulationGameSession ready(
		String playerNickname,
		Long memberId,
		String guestSessionKey,
		PopulationGameLevel gameLevel,
		Integer totalRounds
	) {
		return new PopulationGameSession(UUID.randomUUID(), playerNickname, memberId, guestSessionKey, gameLevel, totalRounds);
	}

	public void planNextStage(Integer nextStageNumber) {
		expandTotalRoundsTo(nextStageNumber);
	}

	public void clearCurrentStage(Integer stageNumber, Integer awardedScore, LocalDateTime clearedAt) {
		assertInProgressRound(stageNumber);
		advanceAfterSuccessfulRound(awardedScore, clearedAt);
	}

	public void recordWrongAttempt(Integer stageNumber, LocalDateTime attemptedAt) {
		assertInProgressRound(stageNumber);
		this.livesRemaining = getLivesRemaining() - 1;

		if (livesRemaining <= 0) {
			this.livesRemaining = 0;
			finish(GameSessionStatus.GAME_OVER, attemptedAt);
		}
	}

	public void restart(Integer totalStages) {
		resetForRestart(totalStages);
		this.livesRemaining = DEFAULT_LIVES;
	}

	public Integer getLivesRemaining() {
		return livesRemaining == null ? DEFAULT_LIVES : livesRemaining;
	}

	public PopulationGameLevel getGameLevel() {
		return gameLevel == null ? PopulationGameLevel.LEVEL_1 : gameLevel;
	}

	public Integer getCurrentStageNumber() {
		return getCurrentRoundNumber();
	}

	public Integer getClearedStageCount() {
		return getAnsweredRoundCount();
	}
}
