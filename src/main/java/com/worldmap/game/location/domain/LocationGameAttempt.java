package com.worldmap.game.location.domain;

import com.worldmap.country.domain.Country;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
	name = "location_game_attempt",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uk_location_game_attempt_stage_attempt",
			columnNames = {"stage_id", "attempt_number"}
		)
	}
)
public class LocationGameAttempt {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "stage_id", nullable = false)
	private LocationGameStage stage;

	@Column(name = "attempt_number", nullable = false)
	private Integer attemptNumber;

	@Column(name = "selected_country_iso3_code", nullable = false, length = 3)
	private String selectedCountryIso3Code;

	@Column(name = "selected_country_name", nullable = false, length = 80)
	private String selectedCountryName;

	@Column(nullable = false)
	private Boolean correct;

	@Column(name = "lives_remaining_after", nullable = false)
	private Integer livesRemainingAfter;

	@Column(name = "attempted_at", nullable = false)
	private LocalDateTime attemptedAt;

	protected LocationGameAttempt() {
	}

	private LocationGameAttempt(
		LocationGameStage stage,
		Integer attemptNumber,
		Country selectedCountry,
		Boolean correct,
		Integer livesRemainingAfter,
		LocalDateTime attemptedAt
	) {
		this.stage = stage;
		this.attemptNumber = attemptNumber;
		this.selectedCountryIso3Code = selectedCountry.getIso3Code();
		this.selectedCountryName = selectedCountry.getNameKr();
		this.correct = correct;
		this.livesRemainingAfter = livesRemainingAfter;
		this.attemptedAt = attemptedAt;
	}

	public static LocationGameAttempt create(
		LocationGameStage stage,
		Integer attemptNumber,
		Country selectedCountry,
		Boolean correct,
		Integer livesRemainingAfter,
		LocalDateTime attemptedAt
	) {
		return new LocationGameAttempt(stage, attemptNumber, selectedCountry, correct, livesRemainingAfter, attemptedAt);
	}

	public Long getId() {
		return id;
	}

	public LocationGameStage getStage() {
		return stage;
	}

	public Integer getAttemptNumber() {
		return attemptNumber;
	}

	public String getSelectedCountryIso3Code() {
		return selectedCountryIso3Code;
	}

	public String getSelectedCountryName() {
		return selectedCountryName;
	}

	public Boolean getCorrect() {
		return correct;
	}

	public Integer getLivesRemainingAfter() {
		return livesRemainingAfter;
	}

	public LocalDateTime getAttemptedAt() {
		return attemptedAt;
	}
}
