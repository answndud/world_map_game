package com.worldmap.recommendation.web;

import com.worldmap.recommendation.application.RecommendationFeedbackSubmission;
import com.worldmap.recommendation.domain.RecommendationSurveyAnswers;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RecommendationFeedbackRequest {

	@NotBlank(message = "surveyVersion은 비어 있을 수 없습니다.")
	private String surveyVersion;

	@NotBlank(message = "engineVersion은 비어 있을 수 없습니다.")
	private String engineVersion;

	@NotNull(message = "만족도 점수를 선택해주세요.")
	@Min(value = 1, message = "만족도 점수는 1점 이상이어야 합니다.")
	@Max(value = 5, message = "만족도 점수는 5점 이하여야 합니다.")
	private Integer satisfactionScore;

	@NotNull(message = "기후 취향이 필요합니다.")
	private RecommendationSurveyAnswers.ClimatePreference climatePreference;

	@NotNull(message = "생활 속도가 필요합니다.")
	private RecommendationSurveyAnswers.PacePreference pacePreference;

	@NotNull(message = "물가 허용 범위가 필요합니다.")
	private RecommendationSurveyAnswers.BudgetPreference budgetPreference;

	@NotNull(message = "환경 취향이 필요합니다.")
	private RecommendationSurveyAnswers.EnvironmentPreference environmentPreference;

	@NotNull(message = "영어 중요도가 필요합니다.")
	private RecommendationSurveyAnswers.EnglishImportance englishImportance;

	@NotNull(message = "최우선 기준이 필요합니다.")
	private RecommendationSurveyAnswers.PriorityFocus priorityFocus;

	@NotNull(message = "정착 성향이 필요합니다.")
	private RecommendationSurveyAnswers.SettlementPreference settlementPreference;

	@NotNull(message = "이동 생활 방식이 필요합니다.")
	private RecommendationSurveyAnswers.MobilityPreference mobilityPreference;

	public RecommendationFeedbackSubmission toSubmission() {
		return new RecommendationFeedbackSubmission(
			surveyVersion,
			engineVersion,
			satisfactionScore,
			new RecommendationSurveyAnswers(
				climatePreference,
				pacePreference,
				budgetPreference,
				environmentPreference,
				englishImportance,
				priorityFocus,
				settlementPreference,
				mobilityPreference
			)
		);
	}

	public String getSurveyVersion() {
		return surveyVersion;
	}

	public void setSurveyVersion(String surveyVersion) {
		this.surveyVersion = surveyVersion;
	}

	public String getEngineVersion() {
		return engineVersion;
	}

	public void setEngineVersion(String engineVersion) {
		this.engineVersion = engineVersion;
	}

	public Integer getSatisfactionScore() {
		return satisfactionScore;
	}

	public void setSatisfactionScore(Integer satisfactionScore) {
		this.satisfactionScore = satisfactionScore;
	}

	public RecommendationSurveyAnswers.ClimatePreference getClimatePreference() {
		return climatePreference;
	}

	public void setClimatePreference(RecommendationSurveyAnswers.ClimatePreference climatePreference) {
		this.climatePreference = climatePreference;
	}

	public RecommendationSurveyAnswers.PacePreference getPacePreference() {
		return pacePreference;
	}

	public void setPacePreference(RecommendationSurveyAnswers.PacePreference pacePreference) {
		this.pacePreference = pacePreference;
	}

	public RecommendationSurveyAnswers.BudgetPreference getBudgetPreference() {
		return budgetPreference;
	}

	public void setBudgetPreference(RecommendationSurveyAnswers.BudgetPreference budgetPreference) {
		this.budgetPreference = budgetPreference;
	}

	public RecommendationSurveyAnswers.EnvironmentPreference getEnvironmentPreference() {
		return environmentPreference;
	}

	public void setEnvironmentPreference(RecommendationSurveyAnswers.EnvironmentPreference environmentPreference) {
		this.environmentPreference = environmentPreference;
	}

	public RecommendationSurveyAnswers.EnglishImportance getEnglishImportance() {
		return englishImportance;
	}

	public void setEnglishImportance(RecommendationSurveyAnswers.EnglishImportance englishImportance) {
		this.englishImportance = englishImportance;
	}

	public RecommendationSurveyAnswers.PriorityFocus getPriorityFocus() {
		return priorityFocus;
	}

	public void setPriorityFocus(RecommendationSurveyAnswers.PriorityFocus priorityFocus) {
		this.priorityFocus = priorityFocus;
	}

	public RecommendationSurveyAnswers.SettlementPreference getSettlementPreference() {
		return settlementPreference;
	}

	public void setSettlementPreference(RecommendationSurveyAnswers.SettlementPreference settlementPreference) {
		this.settlementPreference = settlementPreference;
	}

	public RecommendationSurveyAnswers.MobilityPreference getMobilityPreference() {
		return mobilityPreference;
	}

	public void setMobilityPreference(RecommendationSurveyAnswers.MobilityPreference mobilityPreference) {
		this.mobilityPreference = mobilityPreference;
	}
}
