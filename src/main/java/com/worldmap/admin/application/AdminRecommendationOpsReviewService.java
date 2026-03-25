package com.worldmap.admin.application;

import com.worldmap.recommendation.application.RecommendationFeedbackInsightsView;
import com.worldmap.recommendation.application.RecommendationFeedbackService;
import com.worldmap.recommendation.application.RecommendationFeedbackSummaryView;
import com.worldmap.recommendation.application.RecommendationSurveyService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminRecommendationOpsReviewService {

	private static final long MIN_RESPONSES_FOR_TUNING = 5;
	private static final double LOW_SATISFACTION_THRESHOLD = 3.80;

	private final RecommendationFeedbackService recommendationFeedbackService;
	private final AdminPersonaBaselineService adminPersonaBaselineService;

	public AdminRecommendationOpsReviewService(
		RecommendationFeedbackService recommendationFeedbackService,
		AdminPersonaBaselineService adminPersonaBaselineService
	) {
		this.recommendationFeedbackService = recommendationFeedbackService;
		this.adminPersonaBaselineService = adminPersonaBaselineService;
	}

	@Transactional(readOnly = true)
	public AdminRecommendationOpsReviewView loadReview() {
		RecommendationFeedbackInsightsView feedbackInsights = recommendationFeedbackService.summarizeByVersion();
		AdminPersonaBaselineView personaBaseline = adminPersonaBaselineService.loadBaseline();
		RecommendationFeedbackSummaryView currentVersionSummary = feedbackInsights.versionSummaries().stream()
			.filter(summary -> summary.surveyVersion().equals(RecommendationSurveyService.SURVEY_VERSION))
			.filter(summary -> summary.engineVersion().equals(RecommendationSurveyService.ENGINE_VERSION))
			.findFirst()
			.orElse(null);

		long currentVersionResponseCount = currentVersionSummary == null ? 0L : currentVersionSummary.responseCount();
		double currentVersionAverageSatisfaction = currentVersionSummary == null ? 0.0 : currentVersionSummary.averageSatisfaction();

		OpsPriorityDecision priorityDecision = decidePriority(
			currentVersionResponseCount,
			currentVersionAverageSatisfaction,
			personaBaseline
		);

		return new AdminRecommendationOpsReviewView(
			(int) currentVersionResponseCount,
			currentVersionAverageSatisfaction,
			RecommendationSurveyService.SURVEY_VERSION,
			RecommendationSurveyService.ENGINE_VERSION,
			personaBaseline.matchedScenarioCount(),
			personaBaseline.totalScenarioCount(),
			personaBaseline.weakScenarioCount(),
			personaBaseline.anchorDriftScenarioCount(),
			priorityDecision.title(),
			priorityDecision.reason(),
			priorityDecision.scenarioIds()
		);
	}

	private OpsPriorityDecision decidePriority(
		long currentVersionResponseCount,
		double currentVersionAverageSatisfaction,
		AdminPersonaBaselineView personaBaseline
	) {
		if (currentVersionResponseCount < MIN_RESPONSES_FOR_TUNING) {
			return new OpsPriorityDecision(
				"현재 버전 피드백 더 수집",
				"baseline은 "
					+ personaBaseline.matchedScenarioCount()
					+ " / "
					+ personaBaseline.totalScenarioCount()
					+ "까지 올라왔지만, 현재 버전 응답이 "
					+ currentVersionResponseCount
					+ "개라서 먼저 실제 만족도 표본을 더 모으는 편이 안전합니다.",
				topScenarioIds(personaBaseline.anchorDriftScenarios())
			);
		}

		if (personaBaseline.weakScenarioCount() > 0) {
			return new OpsPriorityDecision(
				"weak scenario 먼저 정리",
				"기대 후보가 top 3에 전혀 들어오지 않는 시나리오가 아직 남아 있으므로, 순위 미세 조정보다 weak scenario 제거를 먼저 보는 편이 낫습니다.",
				topScenarioIds(personaBaseline.weakScenarios())
			);
		}

		if (currentVersionAverageSatisfaction < LOW_SATISFACTION_THRESHOLD) {
			return new OpsPriorityDecision(
				"설문 문구와 helper text 먼저 점검",
				"baseline은 안정적이지만 현재 버전 평균 만족도가 "
					+ String.format("%.2f", currentVersionAverageSatisfaction)
					+ "점이라면, 점수식보다 문항 문구와 안내 카피를 먼저 다듬는 편이 낫습니다.",
				topScenarioIds(personaBaseline.anchorDriftScenarios())
			);
		}

		if (personaBaseline.anchorDriftScenarioCount() > 0) {
			return new OpsPriorityDecision(
				"rank drift 줄이기",
				"weak scenario는 0개지만 기대 1위 anchor가 밀리는 시나리오가 "
					+ personaBaseline.anchorDriftScenarioCount()
					+ "개 남아 있습니다. 다음 tuning은 broad bonus보다 drift를 줄이는 방향이 맞습니다.",
				topScenarioIds(personaBaseline.anchorDriftScenarios())
			);
		}

		return new OpsPriorityDecision(
			"현재 엔진 유지",
			"현재 버전은 weak scenario와 anchor drift 모두 안정적이어서, 다음 개선은 새 피드백이 더 쌓일 때까지 보류해도 됩니다.",
			List.of()
		);
	}

	private List<String> topScenarioIds(List<AdminPersonaBaselineScenarioView> scenarios) {
		return scenarios.stream()
			.map(AdminPersonaBaselineScenarioView::scenarioId)
			.limit(3)
			.toList();
	}

	private record OpsPriorityDecision(
		String title,
		String reason,
		List<String> scenarioIds
	) {
	}
}
