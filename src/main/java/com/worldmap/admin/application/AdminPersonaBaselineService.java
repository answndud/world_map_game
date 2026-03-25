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

		List<AdminPersonaBaselineScenarioView> anchorDriftScenarios = evaluatedScenarios.stream()
			.filter(EvaluatedScenario::matchesAnyExpectedCandidate)
			.filter(evaluated -> !evaluated.matchesExpectedAnchorCandidate())
			.map(this::toAnchorDriftScenarioView)
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
			anchorDriftScenarios.size(),
			weakScenarios,
			anchorDriftScenarios,
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

		boolean matchesExpectedAnchorCandidate = !scenario.expectedCandidates().isEmpty()
			&& !currentTopCandidates.isEmpty()
			&& scenario.expectedCandidates().get(0).equals(currentTopCandidates.get(0));

		return new EvaluatedScenario(
			scenario,
			currentTopCandidates,
			matchesAnyExpectedCandidate,
			matchesExpectedAnchorCandidate
		);
	}

	private AdminPersonaBaselineScenarioView toWeakScenarioView(EvaluatedScenario evaluatedScenario) {
		return new AdminPersonaBaselineScenarioView(
			evaluatedScenario.scenario().id(),
			evaluatedScenario.scenario().description(),
			expectedAnchorCandidate(evaluatedScenario),
			currentAnchorCandidate(evaluatedScenario),
			evaluatedScenario.scenario().expectedCandidates(),
			evaluatedScenario.currentTopCandidates(),
			evaluatedScenario.scenario().analysisNote().isBlank()
				? "기대 후보가 현재 top 3에 들어오지 않았다. 다음 helper text / penalty / bonus 실험 우선 대상으로 본다."
				: evaluatedScenario.scenario().analysisNote()
		);
	}

	private AdminPersonaBaselineScenarioView toAnchorDriftScenarioView(EvaluatedScenario evaluatedScenario) {
		return new AdminPersonaBaselineScenarioView(
			evaluatedScenario.scenario().id(),
			evaluatedScenario.scenario().description(),
			expectedAnchorCandidate(evaluatedScenario),
			currentAnchorCandidate(evaluatedScenario),
			evaluatedScenario.scenario().expectedCandidates(),
			evaluatedScenario.currentTopCandidates(),
			evaluatedScenario.scenario().analysisNote().isBlank()
				? "기대 후보는 top 3에 유지되지만 1위 anchor가 아직 다르다. 다음 tuning은 weak scenario보다 rank drift를 줄이는 방향으로 본다."
				: evaluatedScenario.scenario().analysisNote() + " 현재는 기대 1위 anchor가 top 1이 아니다."
		);
	}

	private AdminPersonaBaselineScenarioView toActiveSignalScenarioView(EvaluatedScenario evaluatedScenario) {
		return new AdminPersonaBaselineScenarioView(
			evaluatedScenario.scenario().id(),
			evaluatedScenario.scenario().description(),
			expectedAnchorCandidate(evaluatedScenario),
			currentAnchorCandidate(evaluatedScenario),
			evaluatedScenario.scenario().expectedCandidates(),
			evaluatedScenario.currentTopCandidates(),
			evaluatedScenario.scenario().analysisNote()
		);
	}

	private String expectedAnchorCandidate(EvaluatedScenario evaluatedScenario) {
		return evaluatedScenario.scenario().expectedCandidates().isEmpty()
			? "-"
			: evaluatedScenario.scenario().expectedCandidates().get(0);
	}

	private String currentAnchorCandidate(EvaluatedScenario evaluatedScenario) {
		return evaluatedScenario.currentTopCandidates().isEmpty()
			? "-"
			: evaluatedScenario.currentTopCandidates().get(0);
	}

	private record EvaluatedScenario(
		RecommendationPersonaBaselineScenario scenario,
		List<String> currentTopCandidates,
		boolean matchesAnyExpectedCandidate,
		boolean matchesExpectedAnchorCandidate
	) {
	}
}
