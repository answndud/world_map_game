package com.worldmap.recommendation.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class RecommendationOfflinePersonaCoverageTest {

	@Autowired
	private RecommendationSurveyService recommendationSurveyService;

	@Test
	void currentEngineMaintainsMinimumPersonaCoverage() {
		List<RecommendationOfflinePersonaScenario> scenarios = RecommendationOfflinePersonaFixtures.scenarios();

		List<String> missedScenarioIds = scenarios.stream()
			.filter(scenario -> !matchesAnyExpectedCandidate(scenario))
			.map(RecommendationOfflinePersonaScenario::id)
			.toList();

		long matchedScenarioCount = scenarios.size() - missedScenarioIds.size();

		assertThat(scenarios).hasSize(18);
		assertThat(matchedScenarioCount)
			.withFailMessage("현재 추천 엔진의 페르소나 coverage가 너무 낮습니다. missed=%s", missedScenarioIds)
			.isGreaterThanOrEqualTo(15);
	}

	@Test
	void anchorAndActiveSignalPersonasStillSurfaceCoreCandidates() {
		assertThat(topCountryNames("P01")).first().isEqualTo("아랍에미리트");
		assertThat(topCountryNames("P01")).contains("싱가포르");
		assertThat(topCountryNames("P02")).first().isEqualTo("태국");
		assertThat(topCountryNames("P02")).contains("스페인");
		assertThat(topCountryNames("P14")).contains("스페인", "태국");
		assertThat(topCountryNames("P15")).contains("뉴질랜드", "말레이시아").doesNotContain("캐나다", "남아프리카 공화국");
		assertThat(topCountryNames("P16")).contains("포르투갈", "뉴질랜드").doesNotContain("말레이시아");
		assertThat(topCountryNames("P17")).contains("싱가포르", "아랍에미리트", "대한민국");
		assertThat(topCountryNames("P18")).contains("싱가포르", "아랍에미리트", "말레이시아").doesNotContain("대한민국");
	}

	private boolean matchesAnyExpectedCandidate(RecommendationOfflinePersonaScenario scenario) {
		List<String> topCountryNames = recommendationSurveyService.recommend(scenario.answers())
			.recommendations()
			.stream()
			.map(RecommendationCandidateView::countryNameKr)
			.toList();

		return scenario.expectedCandidates().stream().anyMatch(topCountryNames::contains);
	}

	private List<String> topCountryNames(String scenarioId) {
		RecommendationOfflinePersonaScenario scenario = RecommendationOfflinePersonaFixtures.scenarios().stream()
			.filter(candidate -> candidate.id().equals(scenarioId))
			.findFirst()
			.orElseThrow();

		return recommendationSurveyService.recommend(scenario.answers())
			.recommendations()
			.stream()
			.map(RecommendationCandidateView::countryNameKr)
			.toList();
	}
}
