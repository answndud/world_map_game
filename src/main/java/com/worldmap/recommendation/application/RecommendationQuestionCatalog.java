package com.worldmap.recommendation.application;

import com.worldmap.recommendation.domain.RecommendationSurveyAnswers;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RecommendationQuestionCatalog {

	public List<RecommendationQuestionView> questions() {
		return List.of(
			new RecommendationQuestionView(
				"climatePreference",
				"오래 머물 때 몸이 가장 편한 날씨는 어느 쪽인가요?",
				"단순히 좋아하는 날씨보다, 몇 달 이상 살아도 컨디션이 무너지지 않을 기후를 떠올리면 됩니다.",
				optionViews(RecommendationSurveyAnswers.ClimatePreference.values())
			),
			new RecommendationQuestionView(
				"seasonTolerance",
				"기후가 조금 극단적이어도 적응할 자신이 있나요?",
				"좋은 인프라나 생활 품질이 있다면 더위, 추위, 계절 차이를 어느 정도 감수할 수 있는지 생각해보세요.",
				optionViews(RecommendationSurveyAnswers.SeasonTolerance.values())
			),
			new RecommendationQuestionView(
				"pacePreference",
				"하루 리듬은 어느 정도 속도가 맞나요?",
				"사람 흐름이 빠른 도시 리듬이 좋은지, 조금 더 숨 돌릴 수 있는 생활이 좋은지 골라주세요.",
				optionViews(RecommendationSurveyAnswers.PacePreference.values())
			),
			new RecommendationQuestionView(
				"costQualityPreference",
				"생활비가 높더라도 인프라·안전·편의가 확실하면 감수할 의향이 있나요?",
				"대부분은 낮은 물가를 좋아합니다. 그래서 이번 질문은 '비용을 더 내더라도 얻고 싶은 생활 품질이 있는가'를 묻습니다.",
				optionViews(RecommendationSurveyAnswers.CostQualityPreference.values())
			),
			new RecommendationQuestionView(
				"environmentPreference",
				"도시 편의와 숨 쉴 공간 중 어느 쪽의 비중이 더 큰가요?",
				"바로 닿는 편의와 자극이 중요한지, 자연과 여백이 일상 만족도에 더 큰지 생각해보세요.",
				optionViews(RecommendationSurveyAnswers.EnvironmentPreference.values())
			),
			new RecommendationQuestionView(
				"englishSupportNeed",
				"초기 적응 단계에서 영어 지원은 어느 정도 필요하신가요?",
				"행정, 생활, 커뮤니티 적응에서 영어만으로도 버틸 수 있어야 안심되는지, 다른 장점이 더 크면 감수 가능한지를 고릅니다.",
				optionViews(RecommendationSurveyAnswers.EnglishSupportNeed.values())
			),
			new RecommendationQuestionView(
				"safetyPriority",
				"치안과 생활 안전은 어느 정도 핵심 조건인가요?",
				"다른 장점이 많더라도 안전 체감이 낮으면 오래 머물기 어렵다고 느끼는지 판단해보세요.",
				optionViews(RecommendationSurveyAnswers.ImportanceLevel.values())
			),
			new RecommendationQuestionView(
				"publicServicePriority",
				"의료·행정·복지 같은 공공 서비스 수준은 어느 정도 중요하신가요?",
				"당장 화려하지 않아도 제도와 기본 서비스가 안정적인 나라를 얼마나 중시하는지 고릅니다.",
				optionViews(RecommendationSurveyAnswers.ImportanceLevel.values())
			),
			new RecommendationQuestionView(
				"foodImportance",
				"음식 만족도와 외식 다양성은 어느 정도 중요한가요?",
				"매일의 식사 만족도가 생활 적응에 큰 영향을 주는지, 다른 요소가 더 큰지 생각해보세요.",
				optionViews(RecommendationSurveyAnswers.ImportanceLevel.values())
			),
			new RecommendationQuestionView(
				"diversityImportance",
				"다문화 분위기와 새로운 사람·문화에 열려 있는 환경은 어느 정도 중요하신가요?",
				"국제적인 분위기, 다양한 배경의 사람들과 어울릴 수 있는 환경을 얼마나 중시하는지 고릅니다.",
				optionViews(RecommendationSurveyAnswers.ImportanceLevel.values())
			),
			new RecommendationQuestionView(
				"settlementPreference",
				"이번 선택은 '가볍게 살아보기'와 '장기 정착 가능성' 중 어디에 더 가까운가요?",
				"지금은 경험이 우선인지, 처음부터 오래 머물 수 있는 조건까지 보고 싶은지에 따라 결과가 달라집니다.",
				optionViews(RecommendationSurveyAnswers.SettlementPreference.values())
			),
			new RecommendationQuestionView(
				"mobilityPreference",
				"이동 방식은 어느 쪽이 더 편한가요?",
				"대중교통과 도보만으로도 충분한 생활이 좋은지, 넓은 공간과 느긋한 이동도 괜찮은지 골라주세요.",
				optionViews(RecommendationSurveyAnswers.MobilityPreference.values())
			)
		);
	}

	public List<RecommendationPreferenceSummaryView> summariesOf(RecommendationSurveyAnswers answers) {
		return List.of(
			new RecommendationPreferenceSummaryView("기후 방향", answers.climatePreference().label()),
			new RecommendationPreferenceSummaryView("기후 적응 성향", answers.seasonTolerance().label()),
			new RecommendationPreferenceSummaryView("생활 속도", answers.pacePreference().label()),
			new RecommendationPreferenceSummaryView("비용·품질 기준", answers.costQualityPreference().label()),
			new RecommendationPreferenceSummaryView("생활 환경", answers.environmentPreference().label()),
			new RecommendationPreferenceSummaryView("영어 지원 필요도", answers.englishSupportNeed().label()),
			new RecommendationPreferenceSummaryView("치안 우선도", answers.safetyPriority().label()),
			new RecommendationPreferenceSummaryView("공공 서비스 우선도", answers.publicServicePriority().label()),
			new RecommendationPreferenceSummaryView("음식 만족도 우선도", answers.foodImportance().label()),
			new RecommendationPreferenceSummaryView("문화 다양성 우선도", answers.diversityImportance().label()),
			new RecommendationPreferenceSummaryView("정착 성향", answers.settlementPreference().label()),
			new RecommendationPreferenceSummaryView("이동 방식", answers.mobilityPreference().label())
		);
	}

	private <T extends Enum<T> & RecommendationSurveyAnswers.SurveyOption> List<RecommendationOptionView> optionViews(T[] options) {
		return List.of(options).stream()
			.map(option -> new RecommendationOptionView(option.name(), option.label(), option.description()))
			.toList();
	}
}
