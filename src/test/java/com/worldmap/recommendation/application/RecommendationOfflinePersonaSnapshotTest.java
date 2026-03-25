package com.worldmap.recommendation.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class RecommendationOfflinePersonaSnapshotTest {

	private static final Map<String, List<String>> ENGINE_V8_TOP3_SNAPSHOT = snapshot();

	@Autowired
	private RecommendationSurveyService recommendationSurveyService;

	@Autowired
	private RecommendationPersonaBaselineCatalog baselineCatalog;

	@Test
	void currentEngineMatchesPersonaTop3Snapshot() {
		for (RecommendationPersonaBaselineScenario scenario : baselineCatalog.scenarios()) {
			List<String> actualTop3 = recommendationSurveyService.recommend(scenario.answers())
				.recommendations()
				.stream()
				.map(RecommendationCandidateView::countryNameKr)
				.limit(3)
				.toList();

			assertThat(actualTop3)
				.as("scenario=%s", scenario.id())
				.containsExactlyElementsOf(ENGINE_V8_TOP3_SNAPSHOT.get(scenario.id()));
		}
	}

	private static Map<String, List<String>> snapshot() {
		Map<String, List<String>> snapshot = new LinkedHashMap<>();
		snapshot.put("P01", List.of("싱가포르", "아랍에미리트", "미국"));
		snapshot.put("P02", List.of("태국", "스페인", "말레이시아"));
		snapshot.put("P03", List.of("노르웨이", "핀란드", "덴마크"));
		snapshot.put("P04", List.of("스페인", "아일랜드", "우루과이"));
		snapshot.put("P05", List.of("싱가포르", "아랍에미리트", "미국"));
		snapshot.put("P06", List.of("스페인", "우루과이", "포르투갈"));
		snapshot.put("P07", List.of("싱가포르", "브라질", "아랍에미리트"));
		snapshot.put("P08", List.of("핀란드", "뉴질랜드", "노르웨이"));
		snapshot.put("P09", List.of("싱가포르", "아랍에미리트", "덴마크"));
		snapshot.put("P10", List.of("대한민국", "미국", "영국"));
		snapshot.put("P11", List.of("아일랜드", "캐나다", "스위스"));
		snapshot.put("P12", List.of("포르투갈", "태국", "말레이시아"));
		snapshot.put("P13", List.of("싱가포르", "아랍에미리트", "미국"));
		snapshot.put("P14", List.of("스페인", "말레이시아", "태국"));
		snapshot.put("P15", List.of("포르투갈", "뉴질랜드", "말레이시아"));
		snapshot.put("P16", List.of("뉴질랜드", "포르투갈", "우루과이"));
		snapshot.put("P17", List.of("싱가포르", "아랍에미리트", "대한민국"));
		snapshot.put("P18", List.of("싱가포르", "아랍에미리트", "말레이시아"));
		return Map.copyOf(snapshot);
	}
}
