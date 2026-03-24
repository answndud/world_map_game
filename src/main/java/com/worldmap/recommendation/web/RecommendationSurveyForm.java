package com.worldmap.recommendation.web;

import com.worldmap.recommendation.domain.RecommendationSurveyAnswers;
import jakarta.validation.constraints.NotNull;

public class RecommendationSurveyForm {

	@NotNull(message = "기후 취향을 선택해주세요.")
	private RecommendationSurveyAnswers.ClimatePreference climatePreference;

	@NotNull(message = "생활 속도를 선택해주세요.")
	private RecommendationSurveyAnswers.PacePreference pacePreference;

	@NotNull(message = "물가 허용 범위를 선택해주세요.")
	private RecommendationSurveyAnswers.BudgetPreference budgetPreference;

	@NotNull(message = "도시/자연 취향을 선택해주세요.")
	private RecommendationSurveyAnswers.EnvironmentPreference environmentPreference;

	@NotNull(message = "영어 중요도를 선택해주세요.")
	private RecommendationSurveyAnswers.EnglishImportance englishImportance;

	@NotNull(message = "가장 중요한 기준을 선택해주세요.")
	private RecommendationSurveyAnswers.PriorityFocus priorityFocus;

	@NotNull(message = "정착 성향을 선택해주세요.")
	private RecommendationSurveyAnswers.SettlementPreference settlementPreference;

	@NotNull(message = "이동 생활 방식을 선택해주세요.")
	private RecommendationSurveyAnswers.MobilityPreference mobilityPreference;

	public RecommendationSurveyAnswers toAnswers() {
		return new RecommendationSurveyAnswers(
			climatePreference,
			pacePreference,
			budgetPreference,
			environmentPreference,
			englishImportance,
			priorityFocus,
			settlementPreference,
			mobilityPreference
		);
	}

	public boolean isSelected(String fieldName, String optionValue) {
		String selectedValue = switch (fieldName) {
			case "climatePreference" -> climatePreference != null ? climatePreference.name() : null;
			case "pacePreference" -> pacePreference != null ? pacePreference.name() : null;
			case "budgetPreference" -> budgetPreference != null ? budgetPreference.name() : null;
			case "environmentPreference" -> environmentPreference != null ? environmentPreference.name() : null;
			case "englishImportance" -> englishImportance != null ? englishImportance.name() : null;
			case "priorityFocus" -> priorityFocus != null ? priorityFocus.name() : null;
			case "settlementPreference" -> settlementPreference != null ? settlementPreference.name() : null;
			case "mobilityPreference" -> mobilityPreference != null ? mobilityPreference.name() : null;
			default -> null;
		};

		return optionValue.equals(selectedValue);
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
