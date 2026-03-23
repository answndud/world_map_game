package com.worldmap.game.location.domain;

import com.worldmap.country.domain.Country;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Entity
@Table(
	name = "location_game_stage",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uk_location_game_stage_session_stage",
			columnNames = {"session_id", "stage_number"}
		)
	}
)
public class LocationGameStage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "session_id", nullable = false)
	private LocationGameSession session;

	@Column(name = "stage_number", nullable = false)
	private Integer stageNumber;

	@Column(name = "country_id", nullable = false)
	private Long countryId;

	@Column(name = "target_country_iso3_code", nullable = false, length = 3)
	private String targetCountryIso3Code;

	@Column(name = "target_country_name", nullable = false, length = 80)
	private String targetCountryName;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private LocationGameStageStatus status;

	@Column(name = "attempt_count", nullable = false)
	private Integer attemptCount;

	@Column(name = "awarded_score")
	private Integer awardedScore;

	@Column(name = "cleared_at")
	private LocalDateTime clearedAt;

	protected LocationGameStage() {
	}

	private LocationGameStage(LocationGameSession session, Integer stageNumber, Country country) {
		this.session = session;
		this.stageNumber = stageNumber;
		this.countryId = country.getId();
		this.targetCountryIso3Code = country.getIso3Code();
		this.targetCountryName = country.getNameKr();
		this.status = LocationGameStageStatus.PENDING;
		this.attemptCount = 0;
	}

	public static LocationGameStage create(LocationGameSession session, Integer stageNumber, Country country) {
		return new LocationGameStage(session, stageNumber, country);
	}

	public int nextAttemptNumber() {
		return attemptCount + 1;
	}

	public void recordAttempt(boolean correct, Integer awardedScore, LocalDateTime attemptedAt) {
		if (status != LocationGameStageStatus.PENDING) {
			throw new IllegalStateException("이미 종료된 Stage입니다.");
		}

		this.attemptCount += 1;

		if (correct) {
			this.status = LocationGameStageStatus.CLEARED;
			this.awardedScore = awardedScore;
			this.clearedAt = attemptedAt;
		}
	}

	public void markFailed() {
		if (status == LocationGameStageStatus.CLEARED) {
			throw new IllegalStateException("이미 클리어한 Stage는 실패 처리할 수 없습니다.");
		}

		this.status = LocationGameStageStatus.FAILED;
	}

	public Long getId() {
		return id;
	}

	public LocationGameSession getSession() {
		return session;
	}

	public Integer getStageNumber() {
		return stageNumber;
	}

	public Long getCountryId() {
		return countryId;
	}

	public String getTargetCountryIso3Code() {
		return targetCountryIso3Code;
	}

	public String getTargetCountryName() {
		return targetCountryName;
	}

	public LocationGameStageStatus getStatus() {
		return status;
	}

	public Integer getAttemptCount() {
		return attemptCount;
	}

	public Integer getAwardedScore() {
		return awardedScore;
	}

	public LocalDateTime getClearedAt() {
		return clearedAt;
	}
}
