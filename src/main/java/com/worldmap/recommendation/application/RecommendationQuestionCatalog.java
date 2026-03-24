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
				"나라의 날씨 방향을 먼저 정하면 추천 후보 풀이 크게 달라집니다.",
				optionViews(RecommendationSurveyAnswers.ClimatePreference.values())
			),
			new RecommendationQuestionView(
				"pacePreference",
				"생활 속도는 어느 정도가 맞나요?",
				"도시 리듬과 여유의 균형은 추천 결과에서 큰 비중을 차지합니다.",
				optionViews(RecommendationSurveyAnswers.PacePreference.values())
			),
			new RecommendationQuestionView(
				"budgetPreference",
				"물가 허용 범위는 어느 정도인가요?",
				"생활비 허용 범위는 상위 국가를 가르는 중요한 기준입니다.",
				optionViews(RecommendationSurveyAnswers.BudgetPreference.values())
			),
			new RecommendationQuestionView(
				"environmentPreference",
				"도시와 자연 중 어느 쪽이 더 중요한가요?",
				"대도시 중심인지, 자연과의 거리까지 보는지에 따라 점수가 달라집니다.",
				optionViews(RecommendationSurveyAnswers.EnvironmentPreference.values())
			),
			new RecommendationQuestionView(
				"englishImportance",
				"영어 친화도는 얼마나 중요한가요?",
				"초기 적응 난도를 낮추고 싶다면 이 가중치가 더 크게 반영됩니다.",
				optionViews(RecommendationSurveyAnswers.EnglishImportance.values())
			),
			new RecommendationQuestionView(
				"priorityFocus",
				"마지막으로 가장 중요한 한 가지를 고른다면?",
				"치안, 복지, 음식, 문화 다양성 중 무엇이 결정타인지 고릅니다.",
				optionViews(RecommendationSurveyAnswers.PriorityFocus.values())
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
			new RecommendationPreferenceSummaryView("최우선 기준", answers.priorityFocus().label())
		);
	}

	private <T extends Enum<T> & RecommendationSurveyAnswers.SurveyOption> List<RecommendationOptionView> optionViews(T[] options) {
		return List.of(options).stream()
			.map(option -> new RecommendationOptionView(option.name(), option.label(), option.description()))
			.toList();
	}
}
