package com.worldmap.game.population.domain;

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
import java.util.List;

@Entity
@Table(
	name = "population_game_round",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uk_population_game_round_session_round",
			columnNames = {"session_id", "round_number"}
		)
	}
)
public class PopulationGameRound {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "session_id", nullable = false)
	private PopulationGameSession session;

	@Column(name = "round_number", nullable = false)
	private Integer roundNumber;

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

	@Column(name = "selected_option_number")
	private Integer selectedOptionNumber;

	@Column(name = "selected_population")
	private Long selectedPopulation;

	@Column(name = "correct")
	private Boolean correct;

	@Column(name = "awarded_score")
	private Integer awardedScore;

	@Column(name = "answered_at")
	private LocalDateTime answeredAt;

	protected PopulationGameRound() {
	}

	private PopulationGameRound(
		PopulationGameSession session,
		Integer roundNumber,
		Country country,
		List<Long> options,
		Integer correctOptionNumber
	) {
		this.session = session;
		this.roundNumber = roundNumber;
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
	}

	public static PopulationGameRound create(
		PopulationGameSession session,
		Integer roundNumber,
		Country country,
		List<Long> options,
		Integer correctOptionNumber
	) {
		return new PopulationGameRound(session, roundNumber, country, options, correctOptionNumber);
	}

	public void submit(Integer selectedOptionNumber, Long selectedPopulation, Boolean correct, Integer awardedScore, LocalDateTime answeredAt) {
		if (isAnswered()) {
			throw new IllegalStateException("이미 제출한 라운드입니다.");
		}

		this.selectedOptionNumber = selectedOptionNumber;
		this.selectedPopulation = selectedPopulation;
		this.correct = correct;
		this.awardedScore = awardedScore;
		this.answeredAt = answeredAt;
	}

	public boolean isAnswered() {
		return answeredAt != null;
	}

	public Long getId() {
		return id;
	}

	public PopulationGameSession getSession() {
		return session;
	}

	public Integer getRoundNumber() {
		return roundNumber;
	}

	public String getTargetCountryName() {
		return targetCountryName;
	}

	public Long getTargetPopulation() {
		return targetPopulation;
	}

	public Integer getPopulationYear() {
		return populationYear;
	}

	public Long getOptionOnePopulation() {
		return optionOnePopulation;
	}

	public Long getOptionTwoPopulation() {
		return optionTwoPopulation;
	}

	public Long getOptionThreePopulation() {
		return optionThreePopulation;
	}

	public Long getOptionFourPopulation() {
		return optionFourPopulation;
	}

	public Integer getCorrectOptionNumber() {
		return correctOptionNumber;
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

	public Integer getAwardedScore() {
		return awardedScore;
	}

	public LocalDateTime getAnsweredAt() {
		return answeredAt;
	}

	public List<Long> getOptions() {
		return List.of(optionOnePopulation, optionTwoPopulation, optionThreePopulation, optionFourPopulation);
	}
}
