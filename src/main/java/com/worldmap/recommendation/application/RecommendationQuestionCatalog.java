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
				"어떤 기후가 더 오래 머물기 편한가요?",
				"오래 머물 때 더 편하게 느껴질 날씨를 떠올리면 됩니다.",
				optionViews(RecommendationSurveyAnswers.ClimatePreference.values())
			),
			new RecommendationQuestionView(
				"pacePreference",
				"생활 속도는 어느 정도가 맞나요?",
				"활기 있는 도시 생활이 좋은지, 조금 더 여유로운 일상이 좋은지 골라주세요.",
				optionViews(RecommendationSurveyAnswers.PacePreference.values())
			),
			new RecommendationQuestionView(
				"budgetPreference",
				"물가 허용 범위는 어느 정도인가요?",
				"생활비 부담을 얼마나 감수할 수 있는지에 따라 추천이 달라집니다.",
				optionViews(RecommendationSurveyAnswers.BudgetPreference.values())
			),
			new RecommendationQuestionView(
				"environmentPreference",
				"도시와 자연 중 어느 쪽이 더 중요한가요?",
				"바쁜 도시 중심이 좋은지, 자연과 가까운 생활이 좋은지 생각해보세요.",
				optionViews(RecommendationSurveyAnswers.EnvironmentPreference.values())
			),
			new RecommendationQuestionView(
				"englishImportance",
				"영어 친화도는 얼마나 중요한가요?",
				"처음 적응할 때 영어가 얼마나 중요할지 기준을 정해보세요.",
				optionViews(RecommendationSurveyAnswers.EnglishImportance.values())
			),
			new RecommendationQuestionView(
				"priorityFocus",
				"마지막으로 가장 중요한 한 가지를 고른다면?",
				"치안, 공공 서비스, 음식, 문화 분위기 중 가장 놓치고 싶지 않은 기준을 고릅니다.",
				optionViews(RecommendationSurveyAnswers.PriorityFocus.values())
			),
			new RecommendationQuestionView(
				"settlementPreference",
				"이번 선택은 어느 쪽에 더 가까운가요?",
				"짧게 살아보는 느낌인지, 오래 머무를 가능성까지 보는지에 따라 결과가 달라집니다.",
				optionViews(RecommendationSurveyAnswers.SettlementPreference.values())
			),
			new RecommendationQuestionView(
				"mobilityPreference",
				"일상 이동 방식은 어느 쪽이 더 편한가요?",
				"대중교통과 도보 중심이 좋은지, 넓은 공간과 느긋한 이동이 좋은지 골라주세요.",
				optionViews(RecommendationSurveyAnswers.MobilityPreference.values())
			)
		);
	}

	public List<RecommendationPreferenceSummaryView> summariesOf(RecommendationSurveyAnswers answers) {
		return List.of(
			new RecommendationPreferenceSummaryView("기후", answers.climatePreference().label()),
			new RecommendationPreferenceSummaryView("생활 속도", answers.pacePreference().label()),
			new RecommendationPreferenceSummaryView("물가 허용", answers.budgetPreference().label()),
			new RecommendationPreferenceSummaryView("환경 취향", answers.environmentPreference().label()),
			new RecommendationPreferenceSummaryView("영어 중요도", answers.englishImportance().label()),
			new RecommendationPreferenceSummaryView("최우선 기준", answers.priorityFocus().label()),
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
