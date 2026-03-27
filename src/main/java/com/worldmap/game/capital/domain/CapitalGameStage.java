package com.worldmap.game.capital.domain;

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
	name = "capital_game_stage",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uk_capital_game_stage_session_stage",
			columnNames = {"session_id", "stage_number"}
		)
	}
)
public class CapitalGameStage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "session_id", nullable = false)
	private CapitalGameSession session;

	@Column(name = "stage_number", nullable = false)
	private Integer stageNumber;

	@Column(name = "country_id", nullable = false)
	private Long countryId;

	@Column(name = "country_iso3_code", nullable = false, length = 3)
	private String countryIso3Code;

	@Column(name = "target_country_name", nullable = false, length = 80)
	private String targetCountryName;

	@Column(name = "target_capital_city", nullable = false, length = 120)
	private String targetCapitalCity;

	@Column(name = "option_one_capital_city", nullable = false, length = 120)
	private String optionOneCapitalCity;

	@Column(name = "option_two_capital_city", nullable = false, length = 120)
	private String optionTwoCapitalCity;

	@Column(name = "option_three_capital_city", nullable = false, length = 120)
	private String optionThreeCapitalCity;

	@Column(name = "option_four_capital_city", nullable = false, length = 120)
	private String optionFourCapitalCity;

	@Column(name = "correct_option_number", nullable = false)
	private Integer correctOptionNumber;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private CapitalGameStageStatus status;

	@Column(name = "attempt_count", nullable = false)
	private Integer attemptCount;

	@Column(name = "awarded_score")
	private Integer awardedScore;

	@Column(name = "cleared_at")
	private LocalDateTime clearedAt;

	protected CapitalGameStage() {
	}

	private CapitalGameStage(
		CapitalGameSession session,
		Integer stageNumber,
		Country country,
		List<String> options,
		Integer correctOptionNumber
	) {
		this.session = session;
		this.stageNumber = stageNumber;
		this.countryId = country.getId();
		this.countryIso3Code = country.getIso3Code();
		this.targetCountryName = country.getNameKr();
		this.targetCapitalCity = country.getCapitalCity();
		this.optionOneCapitalCity = options.get(0);
		this.optionTwoCapitalCity = options.get(1);
		this.optionThreeCapitalCity = options.get(2);
		this.optionFourCapitalCity = options.get(3);
		this.correctOptionNumber = correctOptionNumber;
		this.status = CapitalGameStageStatus.PENDING;
		this.attemptCount = 0;
	}

	public static CapitalGameStage create(
		CapitalGameSession session,
		Integer stageNumber,
		Country country,
		List<String> options,
		Integer correctOptionNumber
	) {
		return new CapitalGameStage(session, stageNumber, country, options, correctOptionNumber);
	}

	public int nextAttemptNumber() {
		return attemptCount + 1;
	}

	public void recordAttempt(boolean correct, Integer awardedScore, LocalDateTime attemptedAt) {
		if (status != CapitalGameStageStatus.PENDING) {
			throw new IllegalStateException("이미 종료된 Stage입니다.");
		}

		this.attemptCount += 1;

		if (correct) {
			this.status = CapitalGameStageStatus.CLEARED;
			this.awardedScore = awardedScore;
			this.clearedAt = attemptedAt;
		}
	}

	public void markFailed() {
		if (status == CapitalGameStageStatus.CLEARED) {
			throw new IllegalStateException("이미 클리어한 Stage는 실패 처리할 수 없습니다.");
		}

		this.status = CapitalGameStageStatus.FAILED;
	}

	public Long getId() {
		return id;
	}

	public CapitalGameSession getSession() {
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

	public String getTargetCapitalCity() {
		return targetCapitalCity;
	}

	public Integer getCorrectOptionNumber() {
		return correctOptionNumber;
	}

	public CapitalGameStageStatus getStatus() {
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
		return List.of(
			optionOneCapitalCity,
			optionTwoCapitalCity,
			optionThreeCapitalCity,
			optionFourCapitalCity
		);
	}
}
