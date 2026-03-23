package com.worldmap.game.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Transient;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.domain.Persistable;

@MappedSuperclass
public abstract class BaseGameSession implements Persistable<UUID> {

	@Id
	private UUID id;

	@Column(name = "player_nickname", nullable = false, length = 20)
	private String playerNickname;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private GameSessionStatus status;

	@Column(name = "total_rounds", nullable = false)
	private Integer totalRounds;

	@Column(name = "current_round_number", nullable = false)
	private Integer currentRoundNumber;

	@Column(name = "answered_round_count", nullable = false)
	private Integer answeredRoundCount;

	@Column(name = "total_score", nullable = false)
	private Integer totalScore;

	@Column(name = "started_at")
	private LocalDateTime startedAt;

	@Column(name = "finished_at")
	private LocalDateTime finishedAt;

	@Transient
	private boolean newSession = true;

	protected BaseGameSession() {
	}

	protected BaseGameSession(UUID id, String playerNickname, Integer totalRounds) {
		this.id = id;
		this.playerNickname = playerNickname;
		this.status = GameSessionStatus.READY;
		this.totalRounds = totalRounds;
		this.currentRoundNumber = 1;
		this.answeredRoundCount = 0;
		this.totalScore = 0;
	}

	public void startGame(LocalDateTime startedAt) {
		if (status != GameSessionStatus.READY) {
			throw new IllegalStateException("이미 시작된 게임입니다.");
		}

		this.status = GameSessionStatus.IN_PROGRESS;
		this.startedAt = startedAt;
	}

	public void completeRound(Integer roundNumber, Integer awardedScore, LocalDateTime finishedAt) {
		assertInProgressRound(roundNumber);
		advanceAfterSuccessfulRound(awardedScore, finishedAt);
	}

	protected void assertInProgressRound(Integer roundNumber) {
		if (status != GameSessionStatus.IN_PROGRESS) {
			throw new IllegalStateException("진행 중인 게임만 답안을 제출할 수 있습니다.");
		}

		if (!currentRoundNumber.equals(roundNumber)) {
			throw new IllegalStateException("현재 진행 중인 라운드와 일치하지 않습니다.");
		}
	}

	protected void advanceAfterSuccessfulRound(Integer awardedScore, LocalDateTime finishedAt) {
		this.answeredRoundCount += 1;
		this.totalScore += awardedScore;

		if (answeredRoundCount >= totalRounds) {
			finish(GameSessionStatus.FINISHED, finishedAt);
			return;
		}

		this.currentRoundNumber += 1;
	}

	protected void expandTotalRoundsTo(Integer plannedTotalRounds) {
		if (plannedTotalRounds == null || plannedTotalRounds < 1) {
			throw new IllegalArgumentException("plannedTotalRounds는 1 이상이어야 합니다.");
		}

		if (plannedTotalRounds > this.totalRounds) {
			this.totalRounds = plannedTotalRounds;
		}
	}

	protected void finish(GameSessionStatus terminalStatus, LocalDateTime finishedAt) {
		if (terminalStatus == GameSessionStatus.READY || terminalStatus == GameSessionStatus.IN_PROGRESS) {
			throw new IllegalArgumentException("종료 상태가 아닙니다.");
		}

		this.status = terminalStatus;
		this.finishedAt = finishedAt;
	}

	protected void resetForRestart(Integer totalRounds) {
		if (totalRounds == null || totalRounds < 1) {
			throw new IllegalArgumentException("totalRounds는 1 이상이어야 합니다.");
		}

		this.status = GameSessionStatus.READY;
		this.totalRounds = totalRounds;
		this.currentRoundNumber = 1;
		this.answeredRoundCount = 0;
		this.totalScore = 0;
		this.startedAt = null;
		this.finishedAt = null;
	}

	@Override
	public UUID getId() {
		return id;
	}

	@Override
	public boolean isNew() {
		return newSession;
	}

	public String getPlayerNickname() {
		return playerNickname;
	}

	public GameSessionStatus getStatus() {
		return status;
	}

	public Integer getTotalRounds() {
		return totalRounds;
	}

	public Integer getCurrentRoundNumber() {
		return currentRoundNumber;
	}

	public Integer getAnsweredRoundCount() {
		return answeredRoundCount;
	}

	public Integer getTotalScore() {
		return totalScore;
	}

	public LocalDateTime getStartedAt() {
		return startedAt;
	}

	public LocalDateTime getFinishedAt() {
		return finishedAt;
	}

	@PostPersist
	@PostLoad
	void markNotNew() {
		this.newSession = false;
	}
}
