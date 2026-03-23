package com.worldmap.game.population.domain;

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
import java.util.List;

@Entity
@Table(
	name = "population_game_stage",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uk_population_game_stage_session_stage",
			columnNames = {"session_id", "stage_number"}
		)
	}
)
public class PopulationGameStage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "session_id", nullable = false)
	private PopulationGameSession session;

	@Column(name = "stage_number", nullable = false)
	private Integer stageNumber;

	@Column(name = "country_id", nullable = false)
	private Long countryId;

	@Column(name = "country_iso3_code", nullable = false, length = 3)
	private String countryIso3Code;

	@Column(name = "target_country_name", nullable = false, length = 80)
	private String targetCountryName;

	@Column(name = "target_population", nullable = false)
	private Long targetPopulation;

	@Column(name = "population_year", nullable = false)
	private Integer populationYear;

	@Column(name = "option_one_population", nullable = false)
	private Long optionOnePopulation;

	@Column(name = "option_two_population", nullable = false)
	private Long optionTwoPopulation;

	@Column(name = "option_three_population", nullable = false)
	private Long optionThreePopulation;

	@Column(name = "option_four_population", nullable = false)
	private Long optionFourPopulation;

	@Column(name = "correct_option_number", nullable = false)
	private Integer correctOptionNumber;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private PopulationGameStageStatus status;

	@Column(name = "attempt_count", nullable = false)
	private Integer attemptCount;

	@Column(name = "awarded_score")
	private Integer awardedScore;

	@Column(name = "cleared_at")
	private LocalDateTime clearedAt;

	protected PopulationGameStage() {
	}

	private PopulationGameStage(
		PopulationGameSession session,
		Integer stageNumber,
		Country country,
		List<Long> options,
		Integer correctOptionNumber
	) {
		this.session = session;
		this.stageNumber = stageNumber;
		this.countryId = country.getId();
		this.countryIso3Code = country.getIso3Code();
		this.targetCountryName = country.getNameKr();
		this.targetPopulation = country.getPopulation();
		this.populationYear = country.getPopulationYear();
		this.optionOnePopulation = options.get(0);
		this.optionTwoPopulation = options.get(1);
		this.optionThreePopulation = options.get(2);
		this.optionFourPopulation = options.get(3);
		this.correctOptionNumber = correctOptionNumber;
		this.status = PopulationGameStageStatus.PENDING;
		this.attemptCount = 0;
	}

	public static PopulationGameStage create(
		PopulationGameSession session,
		Integer stageNumber,
		Country country,
		List<Long> options,
		Integer correctOptionNumber
	) {
		return new PopulationGameStage(session, stageNumber, country, options, correctOptionNumber);
	}

	public int nextAttemptNumber() {
		return attemptCount + 1;
	}

	public void recordAttempt(boolean correct, Integer awardedScore, LocalDateTime attemptedAt) {
		if (status != PopulationGameStageStatus.PENDING) {
			throw new IllegalStateException("이미 종료된 Stage입니다.");
		}

		this.attemptCount += 1;

		if (correct) {
			this.status = PopulationGameStageStatus.CLEARED;
			this.awardedScore = awardedScore;
			this.clearedAt = attemptedAt;
		}
	}

	public void markFailed() {
		if (status == PopulationGameStageStatus.CLEARED) {
			throw new IllegalStateException("이미 클리어한 Stage는 실패 처리할 수 없습니다.");
		}

		this.status = PopulationGameStageStatus.FAILED;
	}

	public Long getId() {
		return id;
	}

	public PopulationGameSession getSession() {
		return session;
	}

	public Integer getStageNumber() {
		return stageNumber;
	}

	public String getTargetCountryName() {
		return targetCountryName;
	}

	public String getCountryIso3Code() {
		return countryIso3Code;
	}

	public Long getTargetPopulation() {
		return targetPopulation;
	}

	public Integer getPopulationYear() {
		return populationYear;
	}

	public Integer getCorrectOptionNumber() {
		return correctOptionNumber;
	}

	public PopulationGameStageStatus getStatus() {
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

	public List<Long> getOptions() {
		return List.of(optionOnePopulation, optionTwoPopulation, optionThreePopulation, optionFourPopulation);
	}
}
