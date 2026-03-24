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
	@Column(name = "season_style_preference", length = 30)
	private RecommendationSurveyAnswers.SeasonStylePreference seasonStylePreference;

	@Enumerated(EnumType.STRING)
	@Column(name = "season_tolerance", length = 30)
	private RecommendationSurveyAnswers.SeasonTolerance seasonTolerance;

	@Enumerated(EnumType.STRING)
	@Column(name = "pace_preference", nullable = false, length = 30)
	private RecommendationSurveyAnswers.PacePreference pacePreference;

	@Enumerated(EnumType.STRING)
	@Column(name = "crowd_preference", length = 30)
	private RecommendationSurveyAnswers.CrowdPreference crowdPreference;

	@Enumerated(EnumType.STRING)
	@Column(name = "cost_quality_preference", length = 40)
	private RecommendationSurveyAnswers.CostQualityPreference costQualityPreference;

	@Enumerated(EnumType.STRING)
	@Column(name = "housing_preference", length = 30)
	private RecommendationSurveyAnswers.HousingPreference housingPreference;

	@Enumerated(EnumType.STRING)
	@Column(name = "environment_preference", nullable = false, length = 30)
	private RecommendationSurveyAnswers.EnvironmentPreference environmentPreference;

	@Enumerated(EnumType.STRING)
	@Column(name = "mobility_preference", length = 30)
	private RecommendationSurveyAnswers.MobilityPreference mobilityPreference;

	@Enumerated(EnumType.STRING)
	@Column(name = "english_support_need", length = 30)
	private RecommendationSurveyAnswers.EnglishSupportNeed englishSupportNeed;

	@Enumerated(EnumType.STRING)
	@Column(name = "newcomer_support_need", length = 30)
	private RecommendationSurveyAnswers.NewcomerSupportNeed newcomerSupportNeed;

	@Enumerated(EnumType.STRING)
	@Column(name = "safety_priority", length = 30)
	private RecommendationSurveyAnswers.ImportanceLevel safetyPriority;

	@Enumerated(EnumType.STRING)
	@Column(name = "public_service_priority", length = 30)
	private RecommendationSurveyAnswers.ImportanceLevel publicServicePriority;

	@Enumerated(EnumType.STRING)
	@Column(name = "digital_convenience_priority", length = 30)
	private RecommendationSurveyAnswers.ImportanceLevel digitalConveniencePriority;

	@Enumerated(EnumType.STRING)
	@Column(name = "food_importance", length = 30)
	private RecommendationSurveyAnswers.ImportanceLevel foodImportance;

	@Enumerated(EnumType.STRING)
	@Column(name = "diversity_importance", length = 30)
	private RecommendationSurveyAnswers.ImportanceLevel diversityImportance;

	@Enumerated(EnumType.STRING)
	@Column(name = "culture_leisure_importance", length = 30)
	private RecommendationSurveyAnswers.ImportanceLevel cultureLeisureImportance;

	@Enumerated(EnumType.STRING)
	@Column(name = "work_life_preference", length = 30)
	private RecommendationSurveyAnswers.WorkLifePreference workLifePreference;

	@Enumerated(EnumType.STRING)
	@Column(name = "settlement_preference", length = 30)
	private RecommendationSurveyAnswers.SettlementPreference settlementPreference;

	@Enumerated(EnumType.STRING)
	@Column(name = "future_base_preference", length = 30)
	private RecommendationSurveyAnswers.FutureBasePreference futureBasePreference;

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
		this.seasonStylePreference = answers.seasonStylePreference();
		this.seasonTolerance = answers.seasonTolerance();
		this.pacePreference = answers.pacePreference();
		this.crowdPreference = answers.crowdPreference();
		this.costQualityPreference = answers.costQualityPreference();
		this.housingPreference = answers.housingPreference();
		this.environmentPreference = answers.environmentPreference();
		this.mobilityPreference = answers.mobilityPreference();
		this.englishSupportNeed = answers.englishSupportNeed();
		this.newcomerSupportNeed = answers.newcomerSupportNeed();
		this.safetyPriority = answers.safetyPriority();
		this.publicServicePriority = answers.publicServicePriority();
		this.digitalConveniencePriority = answers.digitalConveniencePriority();
		this.foodImportance = answers.foodImportance();
		this.diversityImportance = answers.diversityImportance();
		this.cultureLeisureImportance = answers.cultureLeisureImportance();
		this.workLifePreference = answers.workLifePreference();
		this.settlementPreference = answers.settlementPreference();
		this.futureBasePreference = answers.futureBasePreference();
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

	public RecommendationSurveyAnswers.SeasonStylePreference getSeasonStylePreference() {
		return seasonStylePreference;
	}

	public RecommendationSurveyAnswers.SeasonTolerance getSeasonTolerance() {
		return seasonTolerance;
	}

	public RecommendationSurveyAnswers.PacePreference getPacePreference() {
		return pacePreference;
	}

	public RecommendationSurveyAnswers.CrowdPreference getCrowdPreference() {
		return crowdPreference;
	}

	public RecommendationSurveyAnswers.CostQualityPreference getCostQualityPreference() {
		return costQualityPreference;
	}

	public RecommendationSurveyAnswers.HousingPreference getHousingPreference() {
		return housingPreference;
	}

	public RecommendationSurveyAnswers.EnvironmentPreference getEnvironmentPreference() {
		return environmentPreference;
	}

	public RecommendationSurveyAnswers.MobilityPreference getMobilityPreference() {
		return mobilityPreference;
	}

	public RecommendationSurveyAnswers.EnglishSupportNeed getEnglishSupportNeed() {
		return englishSupportNeed;
	}

	public RecommendationSurveyAnswers.NewcomerSupportNeed getNewcomerSupportNeed() {
		return newcomerSupportNeed;
	}

	public RecommendationSurveyAnswers.ImportanceLevel getSafetyPriority() {
		return safetyPriority;
	}

	public RecommendationSurveyAnswers.ImportanceLevel getPublicServicePriority() {
		return publicServicePriority;
	}

	public RecommendationSurveyAnswers.ImportanceLevel getDigitalConveniencePriority() {
		return digitalConveniencePriority;
	}

	public RecommendationSurveyAnswers.ImportanceLevel getFoodImportance() {
		return foodImportance;
	}

	public RecommendationSurveyAnswers.ImportanceLevel getDiversityImportance() {
		return diversityImportance;
	}

	public RecommendationSurveyAnswers.ImportanceLevel getCultureLeisureImportance() {
		return cultureLeisureImportance;
	}

	public RecommendationSurveyAnswers.WorkLifePreference getWorkLifePreference() {
		return workLifePreference;
	}

	public RecommendationSurveyAnswers.SettlementPreference getSettlementPreference() {
		return settlementPreference;
	}

	public RecommendationSurveyAnswers.FutureBasePreference getFutureBasePreference() {
		return futureBasePreference;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
}
