package com.worldmap.admin.application;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AdminPersonaBaselineService {

	public AdminPersonaBaselineView loadBaseline() {
		return new AdminPersonaBaselineView(
			18,
			15,
			3,
			4,
			weakScenarios(),
			activeSignalScenarios()
		);
	}

	private List<AdminPersonaBaselineScenarioView> weakScenarios() {
		return List.of(
			new AdminPersonaBaselineScenarioView(
				"P04",
				"복지와 균형 잡힌 도시 생활을 원하는 중도 성향 직장인",
				List.of("독일", "덴마크", "캐나다"),
				List.of("우루과이", "칠레", "스페인"),
				"`복지 / 공공 서비스`보다 기후와 물가가 더 크게 반영되는 구간이라 다음 helper text와 welfare weight 보정 대상이다."
			),
			new AdminPersonaBaselineScenarioView(
				"P06",
				"비슷한 기후라도 너무 비싸지 않은 나라를 찾는 현실형 사용자",
				List.of("포르투갈", "스페인", "체코"),
				List.of("우루과이", "아일랜드", "말레이시아"),
				"`LOW budget` 제약이 아직 충분히 강하지 않아 저예산 penalty 실험이 먼저 필요한 시나리오다."
			),
			new AdminPersonaBaselineScenarioView(
				"P13",
				"빠른 도시 환경을 원하지만 너무 더운 기후는 싫어하는 사용자",
				List.of("영국", "캐나다", "네덜란드"),
				List.of("미국", "싱가포르", "아랍에미리트"),
				"`MILD climate` 선호보다 영어와 도시성 점수가 강해서 climate mismatch penalty 실험이 필요한 시나리오다."
			)
		);
	}

	private List<AdminPersonaBaselineScenarioView> activeSignalScenarios() {
		return List.of(
			new AdminPersonaBaselineScenarioView(
				"P15",
				"탐색형 저예산 자연 선호",
				List.of("뉴질랜드", "말레이시아", "우루과이"),
				List.of("뉴질랜드", "말레이시아", "우루과이"),
				"`EXPERIENCE / TRANSIT_FIRST`가 들어오면 말레이시아가 top 3에 유지되는지 본다."
			),
			new AdminPersonaBaselineScenarioView(
				"P16",
				"정착형 저예산 자연 선호",
				List.of("뉴질랜드", "우루과이", "포르투갈"),
				List.of("뉴질랜드", "우루과이", "포르투갈"),
				"`STABILITY / SPACE_FIRST`가 들어오면 포르투갈이 다시 상단 후보로 돌아오는지 본다."
			),
			new AdminPersonaBaselineScenarioView(
				"P17",
				"경험 우선 고도시 탐험형",
				List.of("싱가포르", "아랍에미리트", "브라질"),
				List.of("싱가포르", "아랍에미리트", "브라질"),
				"active-signal 문항이 들어와도 경험형 후보인 브라질이 top 3에 남는지 확인한다."
			),
			new AdminPersonaBaselineScenarioView(
				"P18",
				"정착 우선 고도시 현실형",
				List.of("싱가포르", "아랍에미리트", "대한민국"),
				List.of("싱가포르", "아랍에미리트", "대한민국"),
				"같은 기본 취향에서 `STABILITY / SPACE_FIRST`가 대한민국 같은 정착형 후보로 이동시키는지 본다."
			)
		);
	}
}
