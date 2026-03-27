package com.worldmap.game.flag.domain;

import com.worldmap.game.flag.application.FlagQuestionCountryView;
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
	name = "flag_game_stage",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uk_flag_game_stage_session_stage",
			columnNames = {"session_id", "stage_number"}
		)
	}
)
public class FlagGameStage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "session_id", nullable = false)
	private FlagGameSession session;

	@Column(name = "stage_number", nullable = false)
	private Integer stageNumber;

	@Column(name = "country_id", nullable = false)
	private Long countryId;

	@Column(name = "country_iso3_code", nullable = false, length = 3)
	private String countryIso3Code;

	@Column(name = "target_country_name", nullable = false, length = 80)
	private String targetCountryName;

	@Column(name = "target_flag_relative_path", nullable = false, length = 160)
	private String targetFlagRelativePath;

	@Column(name = "option_one_country_name", nullable = false, length = 80)
	private String optionOneCountryName;

	@Column(name = "option_two_country_name", nullable = false, length = 80)
	private String optionTwoCountryName;

	@Column(name = "option_three_country_name", nullable = false, length = 80)
	private String optionThreeCountryName;

	@Column(name = "option_four_country_name", nullable = false, length = 80)
	private String optionFourCountryName;

	@Column(name = "correct_option_number", nullable = false)
	private Integer correctOptionNumber;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private FlagGameStageStatus status;

	@Column(name = "attempt_count", nullable = false)
	private Integer attemptCount;

	@Column(name = "awarded_score")
	private Integer awardedScore;

	@Column(name = "cleared_at")
	private LocalDateTime clearedAt;

	protected FlagGameStage() {
	}

	private FlagGameStage(
		FlagGameSession session,
		Integer stageNumber,
		FlagQuestionCountryView country,
		List<String> options,
		Integer correctOptionNumber
	) {
		this.session = session;
		this.stageNumber = stageNumber;
		this.countryId = country.countryId();
		this.countryIso3Code = country.iso3Code();
		this.targetCountryName = country.countryNameKr();
		this.targetFlagRelativePath = country.flagRelativePath();
		this.optionOneCountryName = options.get(0);
		this.optionTwoCountryName = options.get(1);
		this.optionThreeCountryName = options.get(2);
		this.optionFourCountryName = options.get(3);
		this.correctOptionNumber = correctOptionNumber;
		this.status = FlagGameStageStatus.PENDING;
		this.attemptCount = 0;
	}

	public static FlagGameStage create(
		FlagGameSession session,
		Integer stageNumber,
		FlagQuestionCountryView country,
		List<String> options,
		Integer correctOptionNumber
	) {
		return new FlagGameStage(session, stageNumber, country, options, correctOptionNumber);
	}

	public int nextAttemptNumber() {
		return attemptCount + 1;
	}

	public void recordAttempt(boolean correct, Integer awardedScore, LocalDateTime attemptedAt) {
		if (status != FlagGameStageStatus.PENDING) {
			throw new IllegalStateException("이미 종료된 Stage입니다.");
		}

		this.attemptCount += 1;

		if (correct) {
			this.status = FlagGameStageStatus.CLEARED;
			this.awardedScore = awardedScore;
			this.clearedAt = attemptedAt;
		}
	}

	public void markFailed() {
		if (status == FlagGameStageStatus.CLEARED) {
			throw new IllegalStateException("이미 클리어한 Stage는 실패 처리할 수 없습니다.");
		}

		this.status = FlagGameStageStatus.FAILED;
	}

	public Long getId() {
		return id;
	}

	public FlagGameSession getSession() {
		return session;
	}

	public Integer getStageNumber() {
		return stageNumber;
	}

	public Long getCountryId() {
		return countryId;
	}

	public String getCountryIso3Code() {
		return countryIso3Code;
	}

	public String getTargetCountryName() {
		return targetCountryName;
	}

	public String getTargetFlagRelativePath() {
		return targetFlagRelativePath;
	}

	public Integer getCorrectOptionNumber() {
		return correctOptionNumber;
	}

	public FlagGameStageStatus getStatus() {
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
			optionOneCountryName,
			optionTwoCountryName,
			optionThreeCountryName,
			optionFourCountryName
		);
	}
}
