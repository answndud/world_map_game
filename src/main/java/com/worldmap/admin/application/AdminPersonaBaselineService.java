package com.worldmap.admin.application;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import com.worldmap.recommendation.application.RecommendationCandidateView;
import com.worldmap.recommendation.application.RecommendationPersonaBaselineCatalog;
import com.worldmap.recommendation.application.RecommendationPersonaBaselineScenario;
import com.worldmap.recommendation.application.RecommendationSurveyService;

@Service
public class AdminPersonaBaselineService {

	private final RecommendationPersonaBaselineCatalog baselineCatalog;
	private final RecommendationSurveyService recommendationSurveyService;

	public AdminPersonaBaselineService(
		RecommendationPersonaBaselineCatalog baselineCatalog,
		RecommendationSurveyService recommendationSurveyService
	) {
		this.baselineCatalog = baselineCatalog;
		this.recommendationSurveyService = recommendationSurveyService;
	}

	@Transactional(readOnly = true)
	public AdminPersonaBaselineView loadBaseline() {
		List<EvaluatedScenario> evaluatedScenarios = baselineCatalog.scenarios().stream()
			.map(this::evaluateScenario)
			.toList();

		List<AdminPersonaBaselineScenarioView> weakScenarios = evaluatedScenarios.stream()
			.filter(evaluated -> !evaluated.matchesAnyExpectedCandidate())
			.map(this::toWeakScenarioView)
			.toList();

		List<AdminPersonaBaselineScenarioView> activeSignalScenarios = evaluatedScenarios.stream()
			.filter(evaluated -> evaluated.scenario().activeSignal())
			.map(this::toActiveSignalScenarioView)
			.toList();

		int totalScenarioCount = evaluatedScenarios.size();
		int matchedScenarioCount = (int) evaluatedScenarios.stream()
			.filter(EvaluatedScenario::matchesAnyExpectedCandidate)
			.count();

		return new AdminPersonaBaselineView(
			totalScenarioCount,
			matchedScenarioCount,
			weakScenarios.size(),
			activeSignalScenarios.size(),
			weakScenarios,
			activeSignalScenarios
		);
	}

	private EvaluatedScenario evaluateScenario(RecommendationPersonaBaselineScenario scenario) {
		List<String> currentTopCandidates = recommendationSurveyService.recommend(scenario.answers())
			.recommendations()
			.stream()
			.map(RecommendationCandidateView::countryNameKr)
			.toList();

		boolean matchesAnyExpectedCandidate = scenario.expectedCandidates().stream()
			.anyMatch(currentTopCandidates::contains);

		return new EvaluatedScenario(scenario, currentTopCandidates, matchesAnyExpectedCandidate);
	}

	private AdminPersonaBaselineScenarioView toWeakScenarioView(EvaluatedScenario evaluatedScenario) {
		return new AdminPersonaBaselineScenarioView(
			evaluatedScenario.scenario().id(),
			evaluatedScenario.scenario().description(),
			evaluatedScenario.scenario().expectedCandidates(),
			evaluatedScenario.currentTopCandidates(),
			evaluatedScenario.scenario().analysisNote().isBlank()
				? "기대 후보가 현재 top 3에 들어오지 않았다. 다음 helper text / penalty / bonus 실험 우선 대상으로 본다."
				: evaluatedScenario.scenario().analysisNote()
		);
	}

	private AdminPersonaBaselineScenarioView toActiveSignalScenarioView(EvaluatedScenario evaluatedScenario) {
		return new AdminPersonaBaselineScenarioView(
			evaluatedScenario.scenario().id(),
			evaluatedScenario.scenario().description(),
			evaluatedScenario.scenario().expectedCandidates(),
			evaluatedScenario.currentTopCandidates(),
			evaluatedScenario.scenario().analysisNote()
		);
	}

	private record EvaluatedScenario(
		RecommendationPersonaBaselineScenario scenario,
		List<String> currentTopCandidates,
		boolean matchesAnyExpectedCandidate
	) {
	}
}
