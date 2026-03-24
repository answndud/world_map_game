package com.worldmap.recommendation.web;

import com.worldmap.recommendation.domain.RecommendationSurveyAnswers;
import jakarta.validation.constraints.NotNull;

public class RecommendationSurveyForm {

	@NotNull(message = "기후 취향을 선택해주세요.")
	private RecommendationSurveyAnswers.ClimatePreference climatePreference;

	@NotNull(message = "계절 변화 선호를 선택해주세요.")
	private RecommendationSurveyAnswers.SeasonStylePreference seasonStylePreference;

	@NotNull(message = "기후 적응 성향을 선택해주세요.")
	private RecommendationSurveyAnswers.SeasonTolerance seasonTolerance;

	@NotNull(message = "생활 속도를 선택해주세요.")
	private RecommendationSurveyAnswers.PacePreference pacePreference;

	@NotNull(message = "동네 밀도 선호를 선택해주세요.")
	private RecommendationSurveyAnswers.CrowdPreference crowdPreference;

	@NotNull(message = "비용과 생활 품질의 균형 기준을 선택해주세요.")
	private RecommendationSurveyAnswers.CostQualityPreference costQualityPreference;

	@NotNull(message = "주거 기준을 선택해주세요.")
	private RecommendationSurveyAnswers.HousingPreference housingPreference;

	@NotNull(message = "도시/자연 취향을 선택해주세요.")
	private RecommendationSurveyAnswers.EnvironmentPreference environmentPreference;

	@NotNull(message = "이동 방식을 선택해주세요.")
	private RecommendationSurveyAnswers.MobilityPreference mobilityPreference;

	@NotNull(message = "영어 지원 필요도를 선택해주세요.")
	private RecommendationSurveyAnswers.EnglishSupportNeed englishSupportNeed;

	@NotNull(message = "초기 적응 지원 필요도를 선택해주세요.")
	private RecommendationSurveyAnswers.NewcomerSupportNeed newcomerSupportNeed;

	@NotNull(message = "치안 우선도를 선택해주세요.")
	private RecommendationSurveyAnswers.ImportanceLevel safetyPriority;

	@NotNull(message = "공공 서비스 우선도를 선택해주세요.")
	private RecommendationSurveyAnswers.ImportanceLevel publicServicePriority;

	@NotNull(message = "디지털 생활 편의 우선도를 선택해주세요.")
	private RecommendationSurveyAnswers.ImportanceLevel digitalConveniencePriority;

	@NotNull(message = "음식 만족도 우선도를 선택해주세요.")
	private RecommendationSurveyAnswers.ImportanceLevel foodImportance;

	@NotNull(message = "문화 다양성 우선도를 선택해주세요.")
	private RecommendationSurveyAnswers.ImportanceLevel diversityImportance;

	@NotNull(message = "문화·여가 우선도를 선택해주세요.")
	private RecommendationSurveyAnswers.ImportanceLevel cultureLeisureImportance;

	@NotNull(message = "일·생활 균형 기준을 선택해주세요.")
	private RecommendationSurveyAnswers.WorkLifePreference workLifePreference;

	@NotNull(message = "정착 성향을 선택해주세요.")
	private RecommendationSurveyAnswers.SettlementPreference settlementPreference;

	@NotNull(message = "장기 기반 기준을 선택해주세요.")
	private RecommendationSurveyAnswers.FutureBasePreference futureBasePreference;

	public RecommendationSurveyAnswers toAnswers() {
		return new RecommendationSurveyAnswers(
			climatePreference,
			seasonStylePreference,
			seasonTolerance,
			pacePreference,
			crowdPreference,
			costQualityPreference,
			housingPreference,
			environmentPreference,
			mobilityPreference,
			englishSupportNeed,
			newcomerSupportNeed,
			safetyPriority,
			publicServicePriority,
			digitalConveniencePriority,
			foodImportance,
			diversityImportance,
			cultureLeisureImportance,
			workLifePreference,
			settlementPreference,
			futureBasePreference
		);
	}

	public boolean isSelected(String fieldName, String optionValue) {
		String selectedValue = switch (fieldName) {
			case "climatePreference" -> climatePreference != null ? climatePreference.name() : null;
			case "seasonStylePreference" -> seasonStylePreference != null ? seasonStylePreference.name() : null;
			case "seasonTolerance" -> seasonTolerance != null ? seasonTolerance.name() : null;
			case "pacePreference" -> pacePreference != null ? pacePreference.name() : null;
			case "crowdPreference" -> crowdPreference != null ? crowdPreference.name() : null;
			case "costQualityPreference" -> costQualityPreference != null ? costQualityPreference.name() : null;
			case "housingPreference" -> housingPreference != null ? housingPreference.name() : null;
			case "environmentPreference" -> environmentPreference != null ? environmentPreference.name() : null;
			case "mobilityPreference" -> mobilityPreference != null ? mobilityPreference.name() : null;
			case "englishSupportNeed" -> englishSupportNeed != null ? englishSupportNeed.name() : null;
			case "newcomerSupportNeed" -> newcomerSupportNeed != null ? newcomerSupportNeed.name() : null;
			case "safetyPriority" -> safetyPriority != null ? safetyPriority.name() : null;
			case "publicServicePriority" -> publicServicePriority != null ? publicServicePriority.name() : null;
			case "digitalConveniencePriority" -> digitalConveniencePriority != null ? digitalConveniencePriority.name() : null;
			case "foodImportance" -> foodImportance != null ? foodImportance.name() : null;
			case "diversityImportance" -> diversityImportance != null ? diversityImportance.name() : null;
			case "cultureLeisureImportance" -> cultureLeisureImportance != null ? cultureLeisureImportance.name() : null;
			case "workLifePreference" -> workLifePreference != null ? workLifePreference.name() : null;
			case "settlementPreference" -> settlementPreference != null ? settlementPreference.name() : null;
			case "futureBasePreference" -> futureBasePreference != null ? futureBasePreference.name() : null;
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

	public RecommendationSurveyAnswers.SeasonStylePreference getSeasonStylePreference() {
		return seasonStylePreference;
	}

	public void setSeasonStylePreference(RecommendationSurveyAnswers.SeasonStylePreference seasonStylePreference) {
		this.seasonStylePreference = seasonStylePreference;
	}

	public RecommendationSurveyAnswers.SeasonTolerance getSeasonTolerance() {
		return seasonTolerance;
	}

	public void setSeasonTolerance(RecommendationSurveyAnswers.SeasonTolerance seasonTolerance) {
		this.seasonTolerance = seasonTolerance;
	}

	public RecommendationSurveyAnswers.PacePreference getPacePreference() {
		return pacePreference;
	}

	public void setPacePreference(RecommendationSurveyAnswers.PacePreference pacePreference) {
		this.pacePreference = pacePreference;
	}

	public RecommendationSurveyAnswers.CrowdPreference getCrowdPreference() {
		return crowdPreference;
	}

	public void setCrowdPreference(RecommendationSurveyAnswers.CrowdPreference crowdPreference) {
		this.crowdPreference = crowdPreference;
	}

	public RecommendationSurveyAnswers.CostQualityPreference getCostQualityPreference() {
		return costQualityPreference;
	}

	public void setCostQualityPreference(RecommendationSurveyAnswers.CostQualityPreference costQualityPreference) {
		this.costQualityPreference = costQualityPreference;
	}

	public RecommendationSurveyAnswers.HousingPreference getHousingPreference() {
		return housingPreference;
	}

	public void setHousingPreference(RecommendationSurveyAnswers.HousingPreference housingPreference) {
		this.housingPreference = housingPreference;
	}

	public RecommendationSurveyAnswers.EnvironmentPreference getEnvironmentPreference() {
		return environmentPreference;
	}

	public void setEnvironmentPreference(RecommendationSurveyAnswers.EnvironmentPreference environmentPreference) {
		this.environmentPreference = environmentPreference;
	}

	public RecommendationSurveyAnswers.MobilityPreference getMobilityPreference() {
		return mobilityPreference;
	}

	public void setMobilityPreference(RecommendationSurveyAnswers.MobilityPreference mobilityPreference) {
		this.mobilityPreference = mobilityPreference;
	}

	public RecommendationSurveyAnswers.EnglishSupportNeed getEnglishSupportNeed() {
		return englishSupportNeed;
	}

	public void setEnglishSupportNeed(RecommendationSurveyAnswers.EnglishSupportNeed englishSupportNeed) {
		this.englishSupportNeed = englishSupportNeed;
	}

	public RecommendationSurveyAnswers.NewcomerSupportNeed getNewcomerSupportNeed() {
		return newcomerSupportNeed;
	}

	public void setNewcomerSupportNeed(RecommendationSurveyAnswers.NewcomerSupportNeed newcomerSupportNeed) {
		this.newcomerSupportNeed = newcomerSupportNeed;
	}

	public RecommendationSurveyAnswers.ImportanceLevel getSafetyPriority() {
		return safetyPriority;
	}

	public void setSafetyPriority(RecommendationSurveyAnswers.ImportanceLevel safetyPriority) {
		this.safetyPriority = safetyPriority;
	}

	public RecommendationSurveyAnswers.ImportanceLevel getPublicServicePriority() {
		return publicServicePriority;
	}

	public void setPublicServicePriority(RecommendationSurveyAnswers.ImportanceLevel publicServicePriority) {
		this.publicServicePriority = publicServicePriority;
	}

	public RecommendationSurveyAnswers.ImportanceLevel getDigitalConveniencePriority() {
		return digitalConveniencePriority;
	}

	public void setDigitalConveniencePriority(RecommendationSurveyAnswers.ImportanceLevel digitalConveniencePriority) {
		this.digitalConveniencePriority = digitalConveniencePriority;
	}

	public RecommendationSurveyAnswers.ImportanceLevel getFoodImportance() {
		return foodImportance;
	}

	public void setFoodImportance(RecommendationSurveyAnswers.ImportanceLevel foodImportance) {
		this.foodImportance = foodImportance;
	}

	public RecommendationSurveyAnswers.ImportanceLevel getDiversityImportance() {
		return diversityImportance;
	}

	public void setDiversityImportance(RecommendationSurveyAnswers.ImportanceLevel diversityImportance) {
		this.diversityImportance = diversityImportance;
	}

	public RecommendationSurveyAnswers.ImportanceLevel getCultureLeisureImportance() {
		return cultureLeisureImportance;
	}

	public void setCultureLeisureImportance(RecommendationSurveyAnswers.ImportanceLevel cultureLeisureImportance) {
		this.cultureLeisureImportance = cultureLeisureImportance;
	}

	public RecommendationSurveyAnswers.WorkLifePreference getWorkLifePreference() {
		return workLifePreference;
	}

	public void setWorkLifePreference(RecommendationSurveyAnswers.WorkLifePreference workLifePreference) {
		this.workLifePreference = workLifePreference;
	}

	public RecommendationSurveyAnswers.SettlementPreference getSettlementPreference() {
		return settlementPreference;
	}

	public void setSettlementPreference(RecommendationSurveyAnswers.SettlementPreference settlementPreference) {
		this.settlementPreference = settlementPreference;
	}

	public RecommendationSurveyAnswers.FutureBasePreference getFutureBasePreference() {
		return futureBasePreference;
	}

	public void setFutureBasePreference(RecommendationSurveyAnswers.FutureBasePreference futureBasePreference) {
		this.futureBasePreference = futureBasePreference;
	}
}
