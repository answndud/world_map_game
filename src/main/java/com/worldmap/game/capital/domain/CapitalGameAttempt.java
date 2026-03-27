package com.worldmap.game.capital.domain;

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
	name = "capital_game_attempt",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uk_capital_game_attempt_stage_attempt",
			columnNames = {"stage_id", "attempt_number"}
		)
	}
)
public class CapitalGameAttempt {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "stage_id", nullable = false)
	private CapitalGameStage stage;

	@Column(name = "attempt_number", nullable = false)
	private Integer attemptNumber;

	@Column(name = "selected_option_number", nullable = false)
	private Integer selectedOptionNumber;

	@Column(name = "selected_capital_city", nullable = false, length = 120)
	private String selectedCapitalCity;

	@Column(nullable = false)
	private Boolean correct;

	@Column(name = "lives_remaining_after", nullable = false)
	private Integer livesRemainingAfter;

	@Column(name = "attempted_at", nullable = false)
	private LocalDateTime attemptedAt;

	protected CapitalGameAttempt() {
	}

	private CapitalGameAttempt(
		CapitalGameStage stage,
		Integer attemptNumber,
		Integer selectedOptionNumber,
		String selectedCapitalCity,
		Boolean correct,
		Integer livesRemainingAfter,
		LocalDateTime attemptedAt
	) {
		this.stage = stage;
		this.attemptNumber = attemptNumber;
		this.selectedOptionNumber = selectedOptionNumber;
		this.selectedCapitalCity = selectedCapitalCity;
		this.correct = correct;
		this.livesRemainingAfter = livesRemainingAfter;
		this.attemptedAt = attemptedAt;
	}

	public static CapitalGameAttempt create(
		CapitalGameStage stage,
		Integer attemptNumber,
		Integer selectedOptionNumber,
		String selectedCapitalCity,
		Boolean correct,
		Integer livesRemainingAfter,
		LocalDateTime attemptedAt
	) {
		return new CapitalGameAttempt(
			stage,
			attemptNumber,
			selectedOptionNumber,
			selectedCapitalCity,
			correct,
			livesRemainingAfter,
			attemptedAt
		);
	}

	public CapitalGameStage getStage() {
		return stage;
	}

	public Integer getAttemptNumber() {
		return attemptNumber;
	}

	public Integer getSelectedOptionNumber() {
		return selectedOptionNumber;
	}

	public String getSelectedCapitalCity() {
		return selectedCapitalCity;
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
