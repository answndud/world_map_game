package com.worldmap.recommendation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(
	name = "recommendation_feedback",
	indexes = {
		@Index(name = "idx_recommendation_feedback_survey", columnList = "survey_version, engine_version"),
		@Index(name = "idx_recommendation_feedback_created_at", columnList = "created_at")
	}
)
public class RecommendationFeedback {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "survey_version", nullable = false, length = 40)
	private String surveyVersion;

	@Column(name = "engine_version", nullable = false, length = 40)
	private String engineVersion;

	@Column(name = "satisfaction_score", nullable = false)
	private Integer satisfactionScore;

	@Enumerated(EnumType.STRING)
	@Column(name = "climate_preference", nullable = false, length = 30)
	private RecommendationSurveyAnswers.ClimatePreference climatePreference;

	@Enumerated(EnumType.STRING)
	@Column(name = "season_tolerance", length = 30)
	private RecommendationSurveyAnswers.SeasonTolerance seasonTolerance;

	@Enumerated(EnumType.STRING)
	@Column(name = "pace_preference", nullable = false, length = 30)
	private RecommendationSurveyAnswers.PacePreference pacePreference;

	@Enumerated(EnumType.STRING)
	@Column(name = "cost_quality_preference", length = 40)
	private RecommendationSurveyAnswers.CostQualityPreference costQualityPreference;

	@Enumerated(EnumType.STRING)
	@Column(name = "environment_preference", nullable = false, length = 30)
	private RecommendationSurveyAnswers.EnvironmentPreference environmentPreference;

	@Enumerated(EnumType.STRING)
	@Column(name = "english_support_need", length = 30)
	private RecommendationSurveyAnswers.EnglishSupportNeed englishSupportNeed;

	@Enumerated(EnumType.STRING)
	@Column(name = "safety_priority", length = 30)
	private RecommendationSurveyAnswers.ImportanceLevel safetyPriority;

	@Enumerated(EnumType.STRING)
	@Column(name = "public_service_priority", length = 30)
	private RecommendationSurveyAnswers.ImportanceLevel publicServicePriority;

	@Enumerated(EnumType.STRING)
	@Column(name = "food_importance", length = 30)
	private RecommendationSurveyAnswers.ImportanceLevel foodImportance;

	@Enumerated(EnumType.STRING)
	@Column(name = "diversity_importance", length = 30)
	private RecommendationSurveyAnswers.ImportanceLevel diversityImportance;

	@Enumerated(EnumType.STRING)
	@Column(name = "settlement_preference", length = 30)
	private RecommendationSurveyAnswers.SettlementPreference settlementPreference;

	@Enumerated(EnumType.STRING)
	@Column(name = "mobility_preference", length = 30)
	private RecommendationSurveyAnswers.MobilityPreference mobilityPreference;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	protected RecommendationFeedback() {
	}

	private RecommendationFeedback(
		String surveyVersion,
		String engineVersion,
		Integer satisfactionScore,
		RecommendationSurveyAnswers answers,
		LocalDateTime createdAt
	) {
		this.surveyVersion = surveyVersion;
		this.engineVersion = engineVersion;
		this.satisfactionScore = satisfactionScore;
		this.climatePreference = answers.climatePreference();
		this.seasonTolerance = answers.seasonTolerance();
		this.pacePreference = answers.pacePreference();
		this.costQualityPreference = answers.costQualityPreference();
		this.environmentPreference = answers.environmentPreference();
		this.englishSupportNeed = answers.englishSupportNeed();
		this.safetyPriority = answers.safetyPriority();
		this.publicServicePriority = answers.publicServicePriority();
		this.foodImportance = answers.foodImportance();
		this.diversityImportance = answers.diversityImportance();
		this.settlementPreference = answers.settlementPreference();
		this.mobilityPreference = answers.mobilityPreference();
		this.createdAt = createdAt;
	}

	public static RecommendationFeedback create(
		String surveyVersion,
		String engineVersion,
		Integer satisfactionScore,
		RecommendationSurveyAnswers answers
	) {
		return new RecommendationFeedback(
			surveyVersion,
			engineVersion,
			satisfactionScore,
			answers,
			LocalDateTime.now()
		);
	}

	public Long getId() {
		return id;
	}

	public String getSurveyVersion() {
		return surveyVersion;
	}

	public String getEngineVersion() {
		return engineVersion;
	}

	public Integer getSatisfactionScore() {
		return satisfactionScore;
	}

	public RecommendationSurveyAnswers.ClimatePreference getClimatePreference() {
		return climatePreference;
	}

	public RecommendationSurveyAnswers.SeasonTolerance getSeasonTolerance() {
		return seasonTolerance;
	}

	public RecommendationSurveyAnswers.PacePreference getPacePreference() {
		return pacePreference;
	}

	public RecommendationSurveyAnswers.CostQualityPreference getCostQualityPreference() {
		return costQualityPreference;
	}

	public RecommendationSurveyAnswers.EnvironmentPreference getEnvironmentPreference() {
		return environmentPreference;
	}

	public RecommendationSurveyAnswers.EnglishSupportNeed getEnglishSupportNeed() {
		return englishSupportNeed;
	}

	public RecommendationSurveyAnswers.ImportanceLevel getSafetyPriority() {
		return safetyPriority;
	}

	public RecommendationSurveyAnswers.ImportanceLevel getPublicServicePriority() {
		return publicServicePriority;
	}

	public RecommendationSurveyAnswers.ImportanceLevel getFoodImportance() {
		return foodImportance;
	}

	public RecommendationSurveyAnswers.ImportanceLevel getDiversityImportance() {
		return diversityImportance;
	}

	public RecommendationSurveyAnswers.SettlementPreference getSettlementPreference() {
		return settlementPreference;
	}

	public RecommendationSurveyAnswers.MobilityPreference getMobilityPreference() {
		return mobilityPreference;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
}
