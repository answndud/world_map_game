package com.worldmap.game.population.domain;

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
	name = "population_game_attempt",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uk_population_game_attempt_stage_attempt",
			columnNames = {"stage_id", "attempt_number"}
		)
	}
)
public class PopulationGameAttempt {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "stage_id", nullable = false)
	private PopulationGameStage stage;

	@Column(name = "attempt_number", nullable = false)
	private Integer attemptNumber;

	@Column(name = "selected_option_number", nullable = false)
	private Integer selectedOptionNumber;

	@Column(name = "selected_population", nullable = false)
	private Long selectedPopulation;

	@Column(nullable = false)
	private Boolean correct;

	@Column(name = "lives_remaining_after", nullable = false)
	private Integer livesRemainingAfter;

	@Column(name = "attempted_at", nullable = false)
	private LocalDateTime attemptedAt;

	protected PopulationGameAttempt() {
	}

	private PopulationGameAttempt(
		PopulationGameStage stage,
		Integer attemptNumber,
		Integer selectedOptionNumber,
		Long selectedPopulation,
		Boolean correct,
		Integer livesRemainingAfter,
		LocalDateTime attemptedAt
	) {
		this.stage = stage;
		this.attemptNumber = attemptNumber;
		this.selectedOptionNumber = selectedOptionNumber;
		this.selectedPopulation = selectedPopulation;
		this.correct = correct;
		this.livesRemainingAfter = livesRemainingAfter;
		this.attemptedAt = attemptedAt;
	}

	public static PopulationGameAttempt create(
		PopulationGameStage stage,
		Integer attemptNumber,
		Integer selectedOptionNumber,
		Long selectedPopulation,
		Boolean correct,
		Integer livesRemainingAfter,
		LocalDateTime attemptedAt
	) {
		return new PopulationGameAttempt(
			stage,
			attemptNumber,
			selectedOptionNumber,
			selectedPopulation,
			correct,
			livesRemainingAfter,
			attemptedAt
		);
	}

	public PopulationGameStage getStage() {
		return stage;
	}

	public Integer getAttemptNumber() {
		return attemptNumber;
	}

	public Integer getSelectedOptionNumber() {
		return selectedOptionNumber;
	}

	public Long getSelectedPopulation() {
		return selectedPopulation;
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
