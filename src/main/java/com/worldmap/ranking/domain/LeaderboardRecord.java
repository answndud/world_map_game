package com.worldmap.ranking.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
	name = "leaderboard_record",
	indexes = {
		@Index(name = "idx_leaderboard_mode_level", columnList = "game_mode, game_level"),
		@Index(name = "idx_leaderboard_mode_level_date", columnList = "game_mode, game_level, leaderboard_date")
	}
)
public class LeaderboardRecord {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "run_signature", nullable = false, unique = true, length = 160)
	private String runSignature;

	@Column(name = "session_id", nullable = false)
	private UUID sessionId;

	@Enumerated(EnumType.STRING)
	@Column(name = "game_mode", nullable = false, length = 20)
	private LeaderboardGameMode gameMode;

	@Enumerated(EnumType.STRING)
	@Column(name = "game_level", nullable = false, length = 20)
	private LeaderboardGameLevel gameLevel;

	@Column(name = "player_nickname", nullable = false, length = 20)
	private String playerNickname;

	@Column(name = "total_score", nullable = false)
	private Integer totalScore;

	@Column(name = "ranking_score", nullable = false)
	private Long rankingScore;

	@Column(name = "cleared_stage_count", nullable = false)
	private Integer clearedStageCount;

	@Column(name = "total_attempt_count", nullable = false)
	private Integer totalAttemptCount;

	@Column(name = "leaderboard_date", nullable = false)
	private LocalDate leaderboardDate;

	@Column(name = "finished_at", nullable = false)
	private LocalDateTime finishedAt;

	protected LeaderboardRecord() {
	}

	private LeaderboardRecord(
		String runSignature,
		UUID sessionId,
		LeaderboardGameMode gameMode,
		LeaderboardGameLevel gameLevel,
		String playerNickname,
		Integer totalScore,
		Long rankingScore,
		Integer clearedStageCount,
		Integer totalAttemptCount,
		LocalDate leaderboardDate,
		LocalDateTime finishedAt
	) {
		this.runSignature = runSignature;
		this.sessionId = sessionId;
		this.gameMode = gameMode;
		this.gameLevel = gameLevel;
		this.playerNickname = playerNickname;
		this.totalScore = totalScore;
		this.rankingScore = rankingScore;
		this.clearedStageCount = clearedStageCount;
		this.totalAttemptCount = totalAttemptCount;
		this.leaderboardDate = leaderboardDate;
		this.finishedAt = finishedAt;
	}

	public static LeaderboardRecord create(
		String runSignature,
		UUID sessionId,
		LeaderboardGameMode gameMode,
		LeaderboardGameLevel gameLevel,
		String playerNickname,
		Integer totalScore,
		Long rankingScore,
		Integer clearedStageCount,
		Integer totalAttemptCount,
		LocalDateTime finishedAt
	) {
		return new LeaderboardRecord(
			runSignature,
			sessionId,
			gameMode,
			gameLevel,
			playerNickname,
			totalScore,
			rankingScore,
			clearedStageCount,
			totalAttemptCount,
			finishedAt.toLocalDate(),
			finishedAt
		);
	}

	public Long getId() {
		return id;
	}

	public String getRunSignature() {
		return runSignature;
	}

	public UUID getSessionId() {
		return sessionId;
	}

	public LeaderboardGameMode getGameMode() {
		return gameMode;
	}

	public LeaderboardGameLevel getGameLevel() {
		return gameLevel;
	}

	public String getPlayerNickname() {
		return playerNickname;
	}

	public Integer getTotalScore() {
		return totalScore;
	}

	public Long getRankingScore() {
		return rankingScore;
	}

	public Integer getClearedStageCount() {
		return clearedStageCount;
	}

	public Integer getTotalAttemptCount() {
		return totalAttemptCount;
	}

	public LocalDate getLeaderboardDate() {
		return leaderboardDate;
	}

	public LocalDateTime getFinishedAt() {
		return finishedAt;
	}
}
