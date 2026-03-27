package com.worldmap.game.populationbattle.domain;

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
	name = "population_battle_game_attempt",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uk_population_battle_game_attempt_stage_attempt",
			columnNames = {"stage_id", "attempt_number"}
		)
	}
)
public class PopulationBattleGameAttempt {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "stage_id", nullable = false)
	private PopulationBattleGameStage stage;

	@Column(name = "attempt_number", nullable = false)
	private Integer attemptNumber;

	@Column(name = "selected_option_number", nullable = false)
	private Integer selectedOptionNumber;

	@Column(name = "selected_country_name", nullable = false, length = 80)
	private String selectedCountryName;

	@Column(nullable = false)
	private Boolean correct;

	@Column(name = "lives_remaining_after", nullable = false)
	private Integer livesRemainingAfter;

	@Column(name = "attempted_at", nullable = false)
	private LocalDateTime attemptedAt;

	protected PopulationBattleGameAttempt() {
	}

	private PopulationBattleGameAttempt(
		PopulationBattleGameStage stage,
		Integer attemptNumber,
		Integer selectedOptionNumber,
		String selectedCountryName,
		Boolean correct,
		Integer livesRemainingAfter,
		LocalDateTime attemptedAt
	) {
		this.stage = stage;
		this.attemptNumber = attemptNumber;
		this.selectedOptionNumber = selectedOptionNumber;
		this.selectedCountryName = selectedCountryName;
		this.correct = correct;
		this.livesRemainingAfter = livesRemainingAfter;
		this.attemptedAt = attemptedAt;
	}

	public static PopulationBattleGameAttempt create(
		PopulationBattleGameStage stage,
		Integer attemptNumber,
		Integer selectedOptionNumber,
		String selectedCountryName,
		Boolean correct,
		Integer livesRemainingAfter,
		LocalDateTime attemptedAt
	) {
		return new PopulationBattleGameAttempt(
			stage,
			attemptNumber,
			selectedOptionNumber,
			selectedCountryName,
			correct,
			livesRemainingAfter,
			attemptedAt
		);
	}

	public PopulationBattleGameStage getStage() {
		return stage;
	}

	public Integer getAttemptNumber() {
		return attemptNumber;
	}

	public Integer getSelectedOptionNumber() {
		return selectedOptionNumber;
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
