package com.worldmap.game.populationbattle.domain;

import com.worldmap.game.populationbattle.application.PopulationBattleRoundOptions;
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
	name = "population_battle_game_stage",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uk_population_battle_game_stage_session_stage",
			columnNames = {"session_id", "stage_number"}
		)
	}
)
public class PopulationBattleGameStage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "session_id", nullable = false)
	private PopulationBattleGameSession session;

	@Column(name = "stage_number", nullable = false)
	private Integer stageNumber;

	@Column(name = "question_prompt", nullable = false, length = 120)
	private String questionPrompt;

	@Column(name = "option_one_country_id", nullable = false)
	private Long optionOneCountryId;

	@Column(name = "option_one_country_iso3_code", nullable = false, length = 3)
	private String optionOneCountryIso3Code;

	@Column(name = "option_one_country_name", nullable = false, length = 80)
	private String optionOneCountryName;

	@Column(name = "option_one_population", nullable = false)
	private Long optionOnePopulation;

	@Column(name = "option_two_country_id", nullable = false)
	private Long optionTwoCountryId;

	@Column(name = "option_two_country_iso3_code", nullable = false, length = 3)
	private String optionTwoCountryIso3Code;

	@Column(name = "option_two_country_name", nullable = false, length = 80)
	private String optionTwoCountryName;

	@Column(name = "option_two_population", nullable = false)
	private Long optionTwoPopulation;

	@Column(name = "correct_option_number", nullable = false)
	private Integer correctOptionNumber;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private PopulationBattleGameStageStatus status;

	@Column(name = "attempt_count", nullable = false)
	private Integer attemptCount;

	@Column(name = "awarded_score")
	private Integer awardedScore;

	@Column(name = "cleared_at")
	private LocalDateTime clearedAt;

	protected PopulationBattleGameStage() {
	}

	private PopulationBattleGameStage(
		PopulationBattleGameSession session,
		Integer stageNumber,
		String questionPrompt,
		PopulationBattleRoundOptions options,
		Integer correctOptionNumber
	) {
		this.session = session;
		this.stageNumber = stageNumber;
		this.questionPrompt = questionPrompt;
		this.optionOneCountryId = options.optionOneCountry().getId();
		this.optionOneCountryIso3Code = options.optionOneCountry().getIso3Code();
		this.optionOneCountryName = options.optionOneCountry().getNameKr();
		this.optionOnePopulation = options.optionOneCountry().getPopulation();
		this.optionTwoCountryId = options.optionTwoCountry().getId();
		this.optionTwoCountryIso3Code = options.optionTwoCountry().getIso3Code();
		this.optionTwoCountryName = options.optionTwoCountry().getNameKr();
		this.optionTwoPopulation = options.optionTwoCountry().getPopulation();
		this.correctOptionNumber = correctOptionNumber;
		this.status = PopulationBattleGameStageStatus.PENDING;
		this.attemptCount = 0;
	}

	public static PopulationBattleGameStage create(
		PopulationBattleGameSession session,
		Integer stageNumber,
		String questionPrompt,
		PopulationBattleRoundOptions options,
		Integer correctOptionNumber
	) {
		return new PopulationBattleGameStage(session, stageNumber, questionPrompt, options, correctOptionNumber);
	}

	public int nextAttemptNumber() {
		return attemptCount + 1;
	}

	public void recordAttempt(boolean correct, Integer awardedScore, LocalDateTime attemptedAt) {
		if (status != PopulationBattleGameStageStatus.PENDING) {
			throw new IllegalStateException("이미 종료된 Stage입니다.");
		}

		this.attemptCount += 1;
		if (correct) {
			this.status = PopulationBattleGameStageStatus.CLEARED;
			this.awardedScore = awardedScore;
			this.clearedAt = attemptedAt;
		}
	}

	public void markFailed() {
		if (status == PopulationBattleGameStageStatus.CLEARED) {
			throw new IllegalStateException("이미 클리어한 Stage는 실패 처리할 수 없습니다.");
		}
		this.status = PopulationBattleGameStageStatus.FAILED;
	}

	public String optionName(int optionNumber) {
		return optionNumber == 1 ? optionOneCountryName : optionTwoCountryName;
	}

	public Long optionPopulation(int optionNumber) {
		return optionNumber == 1 ? optionOnePopulation : optionTwoPopulation;
	}

	public String pairSignature() {
		return optionOneCountryIso3Code.compareTo(optionTwoCountryIso3Code) <= 0
			? optionOneCountryIso3Code + ":" + optionTwoCountryIso3Code
			: optionTwoCountryIso3Code + ":" + optionOneCountryIso3Code;
	}

	public Long getId() {
		return id;
	}

	public PopulationBattleGameSession getSession() {
		return session;
	}

	public Integer getStageNumber() {
		return stageNumber;
	}

	public String getQuestionPrompt() {
		return questionPrompt;
	}

	public Long getOptionOneCountryId() {
		return optionOneCountryId;
	}

	public String getOptionOneCountryIso3Code() {
		return optionOneCountryIso3Code;
	}

	public String getOptionOneCountryName() {
		return optionOneCountryName;
	}

	public Long getOptionOnePopulation() {
		return optionOnePopulation;
	}

	public Long getOptionTwoCountryId() {
		return optionTwoCountryId;
	}

	public String getOptionTwoCountryIso3Code() {
		return optionTwoCountryIso3Code;
	}

	public String getOptionTwoCountryName() {
		return optionTwoCountryName;
	}

	public Long getOptionTwoPopulation() {
		return optionTwoPopulation;
	}

	public Integer getCorrectOptionNumber() {
		return correctOptionNumber;
	}

	public PopulationBattleGameStageStatus getStatus() {
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

	public List<String> getOptions() {
		return List.of(optionOneCountryName, optionTwoCountryName);
	}

	public String getCorrectCountryName() {
		return correctOptionNumber == 1 ? optionOneCountryName : optionTwoCountryName;
	}

	public Long getCorrectCountryPopulation() {
		return correctOptionNumber == 1 ? optionOnePopulation : optionTwoPopulation;
	}
}
