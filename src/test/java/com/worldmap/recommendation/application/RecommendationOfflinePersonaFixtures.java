package com.worldmap.recommendation.application;

import com.worldmap.recommendation.domain.RecommendationSurveyAnswers;
import java.util.List;

final class RecommendationOfflinePersonaFixtures {

	private RecommendationOfflinePersonaFixtures() {
	}

	static List<RecommendationOfflinePersonaScenario> scenarios() {
		return List.of(
			scenario("P01", "따뜻한 기후와 빠른 도시 리듬, 영어 환경을 함께 원하는 디지털 노마드",
				RecommendationSurveyAnswers.ClimatePreference.WARM,
				RecommendationSurveyAnswers.SeasonTolerance.HIGH,
				RecommendationSurveyAnswers.PacePreference.FAST,
				RecommendationSurveyAnswers.CostQualityPreference.QUALITY_FIRST,
				RecommendationSurveyAnswers.EnvironmentPreference.CITY,
				RecommendationSurveyAnswers.EnglishSupportNeed.HIGH,
				PrimaryFocus.DIVERSITY,
				RecommendationSurveyAnswers.SettlementPreference.BALANCED,
				RecommendationSurveyAnswers.MobilityPreference.BALANCED,
				List.of("싱가포르", "아랍에미리트", "미국"), "4~5점"),
			scenario("P02", "생활비를 아끼면서도 음식과 다문화 환경을 선호하는 초보 이민 관심자",
				RecommendationSurveyAnswers.ClimatePreference.WARM,
				RecommendationSurveyAnswers.SeasonTolerance.MEDIUM,
				RecommendationSurveyAnswers.PacePreference.BALANCED,
				RecommendationSurveyAnswers.CostQualityPreference.VALUE_FIRST,
				RecommendationSurveyAnswers.EnvironmentPreference.MIXED,
				RecommendationSurveyAnswers.EnglishSupportNeed.MEDIUM,
				PrimaryFocus.FOOD,
				RecommendationSurveyAnswers.SettlementPreference.BALANCED,
				RecommendationSurveyAnswers.MobilityPreference.BALANCED,
				List.of("말레이시아", "태국", "멕시코"), "4~5점"),
			scenario("P03", "조용하고 안전한 북유럽형 생활을 선호하는 안정 중시형",
				RecommendationSurveyAnswers.ClimatePreference.COLD,
				RecommendationSurveyAnswers.SeasonTolerance.HIGH,
				RecommendationSurveyAnswers.PacePreference.RELAXED,
				RecommendationSurveyAnswers.CostQualityPreference.QUALITY_FIRST,
				RecommendationSurveyAnswers.EnvironmentPreference.NATURE,
				RecommendationSurveyAnswers.EnglishSupportNeed.HIGH,
				PrimaryFocus.SAFETY,
				RecommendationSurveyAnswers.SettlementPreference.BALANCED,
				RecommendationSurveyAnswers.MobilityPreference.BALANCED,
				List.of("노르웨이", "핀란드", "스웨덴"), "4~5점"),
			scenario("P04", "복지와 균형 잡힌 도시 생활을 원하는 중도 성향 직장인",
				RecommendationSurveyAnswers.ClimatePreference.MILD,
				RecommendationSurveyAnswers.SeasonTolerance.LOW,
				RecommendationSurveyAnswers.PacePreference.BALANCED,
				RecommendationSurveyAnswers.CostQualityPreference.BALANCED,
				RecommendationSurveyAnswers.EnvironmentPreference.MIXED,
				RecommendationSurveyAnswers.EnglishSupportNeed.MEDIUM,
				PrimaryFocus.PUBLIC_SERVICE,
				RecommendationSurveyAnswers.SettlementPreference.BALANCED,
				RecommendationSurveyAnswers.MobilityPreference.BALANCED,
				List.of("우루과이", "칠레", "스페인"), "3~4점"),
			scenario("P05", "영어가 매우 중요하고, 도시 활동성과 문화 다양성을 최우선으로 보는 인턴 준비생",
				RecommendationSurveyAnswers.ClimatePreference.WARM,
				RecommendationSurveyAnswers.SeasonTolerance.HIGH,
				RecommendationSurveyAnswers.PacePreference.FAST,
				RecommendationSurveyAnswers.CostQualityPreference.QUALITY_FIRST,
				RecommendationSurveyAnswers.EnvironmentPreference.CITY,
				RecommendationSurveyAnswers.EnglishSupportNeed.HIGH,
				PrimaryFocus.DIVERSITY,
				RecommendationSurveyAnswers.SettlementPreference.BALANCED,
				RecommendationSurveyAnswers.MobilityPreference.BALANCED,
				List.of("싱가포르", "아랍에미리트", "미국"), "4~5점"),
			scenario("P06", "비슷한 기후라도 너무 비싸지 않은 나라를 찾는 현실형 사용자",
				RecommendationSurveyAnswers.ClimatePreference.MILD,
				RecommendationSurveyAnswers.SeasonTolerance.LOW,
				RecommendationSurveyAnswers.PacePreference.BALANCED,
				RecommendationSurveyAnswers.CostQualityPreference.VALUE_FIRST,
				RecommendationSurveyAnswers.EnvironmentPreference.MIXED,
				RecommendationSurveyAnswers.EnglishSupportNeed.MEDIUM,
				PrimaryFocus.SAFETY,
				RecommendationSurveyAnswers.SettlementPreference.BALANCED,
				RecommendationSurveyAnswers.MobilityPreference.BALANCED,
				List.of("우루과이", "아일랜드", "캐나다"), "3~4점"),
			scenario("P07", "음식과 도시 속도가 중요하고, 아시아권 대도시를 선호하는 사용자",
				RecommendationSurveyAnswers.ClimatePreference.WARM,
				RecommendationSurveyAnswers.SeasonTolerance.HIGH,
				RecommendationSurveyAnswers.PacePreference.FAST,
				RecommendationSurveyAnswers.CostQualityPreference.BALANCED,
				RecommendationSurveyAnswers.EnvironmentPreference.CITY,
				RecommendationSurveyAnswers.EnglishSupportNeed.LOW,
				PrimaryFocus.FOOD,
				RecommendationSurveyAnswers.SettlementPreference.BALANCED,
				RecommendationSurveyAnswers.MobilityPreference.BALANCED,
				List.of("일본", "대한민국", "싱가포르"), "4~5점"),
			scenario("P08", "자연과 여유를 좋아하고, 영어는 있으면 좋지만 절대 기준은 아닌 사용자",
				RecommendationSurveyAnswers.ClimatePreference.COLD,
				RecommendationSurveyAnswers.SeasonTolerance.MEDIUM,
				RecommendationSurveyAnswers.PacePreference.RELAXED,
				RecommendationSurveyAnswers.CostQualityPreference.BALANCED,
				RecommendationSurveyAnswers.EnvironmentPreference.NATURE,
				RecommendationSurveyAnswers.EnglishSupportNeed.MEDIUM,
				PrimaryFocus.SAFETY,
				RecommendationSurveyAnswers.SettlementPreference.BALANCED,
				RecommendationSurveyAnswers.MobilityPreference.BALANCED,
				List.of("뉴질랜드", "캐나다", "스웨덴"), "4~5점"),
			scenario("P09", "따뜻한 기후와 높은 비용 감수로 인프라와 안정성을 우선하는 사용자",
				RecommendationSurveyAnswers.ClimatePreference.WARM,
				RecommendationSurveyAnswers.SeasonTolerance.HIGH,
				RecommendationSurveyAnswers.PacePreference.BALANCED,
				RecommendationSurveyAnswers.CostQualityPreference.QUALITY_FIRST,
				RecommendationSurveyAnswers.EnvironmentPreference.CITY,
				RecommendationSurveyAnswers.EnglishSupportNeed.HIGH,
				PrimaryFocus.PUBLIC_SERVICE,
				RecommendationSurveyAnswers.SettlementPreference.BALANCED,
				RecommendationSurveyAnswers.MobilityPreference.BALANCED,
				List.of("아랍에미리트", "싱가포르", "호주"), "4~5점"),
			scenario("P10", "영어 친화도는 낮아도 괜찮고, 문화 다양성과 활기만 있으면 되는 사용자",
				RecommendationSurveyAnswers.ClimatePreference.MILD,
				RecommendationSurveyAnswers.SeasonTolerance.MEDIUM,
				RecommendationSurveyAnswers.PacePreference.FAST,
				RecommendationSurveyAnswers.CostQualityPreference.BALANCED,
				RecommendationSurveyAnswers.EnvironmentPreference.CITY,
				RecommendationSurveyAnswers.EnglishSupportNeed.LOW,
				PrimaryFocus.DIVERSITY,
				RecommendationSurveyAnswers.SettlementPreference.BALANCED,
				RecommendationSurveyAnswers.MobilityPreference.BALANCED,
				List.of("미국", "멕시코", "브라질"), "3~4점"),
			scenario("P11", "치안과 복지를 강하게 보고, 도시와 자연의 균형도 원하는 가족형 사용자",
				RecommendationSurveyAnswers.ClimatePreference.MILD,
				RecommendationSurveyAnswers.SeasonTolerance.LOW,
				RecommendationSurveyAnswers.PacePreference.BALANCED,
				RecommendationSurveyAnswers.CostQualityPreference.QUALITY_FIRST,
				RecommendationSurveyAnswers.EnvironmentPreference.MIXED,
				RecommendationSurveyAnswers.EnglishSupportNeed.HIGH,
				PrimaryFocus.SAFETY,
				RecommendationSurveyAnswers.SettlementPreference.BALANCED,
				RecommendationSurveyAnswers.MobilityPreference.BALANCED,
				List.of("캐나다", "덴마크", "네덜란드"), "4~5점"),
			scenario("P12", "비용 부담은 낮아야 하고, 느긋한 생활과 자연이 중요한 장기 체류 관심자",
				RecommendationSurveyAnswers.ClimatePreference.WARM,
				RecommendationSurveyAnswers.SeasonTolerance.MEDIUM,
				RecommendationSurveyAnswers.PacePreference.RELAXED,
				RecommendationSurveyAnswers.CostQualityPreference.VALUE_FIRST,
				RecommendationSurveyAnswers.EnvironmentPreference.NATURE,
				RecommendationSurveyAnswers.EnglishSupportNeed.MEDIUM,
				PrimaryFocus.SAFETY,
				RecommendationSurveyAnswers.SettlementPreference.BALANCED,
				RecommendationSurveyAnswers.MobilityPreference.BALANCED,
				List.of("포르투갈", "우루과이", "뉴질랜드"), "3~4점"),
			scenario("P13", "빠른 도시 환경과 다문화 경험을 원하지만 너무 더운 기후는 싫어하는 사용자",
				RecommendationSurveyAnswers.ClimatePreference.MILD,
				RecommendationSurveyAnswers.SeasonTolerance.LOW,
				RecommendationSurveyAnswers.PacePreference.FAST,
				RecommendationSurveyAnswers.CostQualityPreference.QUALITY_FIRST,
				RecommendationSurveyAnswers.EnvironmentPreference.CITY,
				RecommendationSurveyAnswers.EnglishSupportNeed.HIGH,
				PrimaryFocus.DIVERSITY,
				RecommendationSurveyAnswers.SettlementPreference.BALANCED,
				RecommendationSurveyAnswers.MobilityPreference.BALANCED,
				List.of("미국", "영국", "싱가포르"), "4~5점"),
			scenario("P14", "아시아권에서 생활비와 편의성 균형을 찾는 실용형 사용자",
				RecommendationSurveyAnswers.ClimatePreference.WARM,
				RecommendationSurveyAnswers.SeasonTolerance.MEDIUM,
				RecommendationSurveyAnswers.PacePreference.BALANCED,
				RecommendationSurveyAnswers.CostQualityPreference.VALUE_FIRST,
				RecommendationSurveyAnswers.EnvironmentPreference.MIXED,
				RecommendationSurveyAnswers.EnglishSupportNeed.MEDIUM,
				PrimaryFocus.PUBLIC_SERVICE,
				RecommendationSurveyAnswers.SettlementPreference.BALANCED,
				RecommendationSurveyAnswers.MobilityPreference.BALANCED,
				List.of("말레이시아", "태국", "베트남"), "4~5점"),
			scenario("P15", "저예산 자연형이지만 먼저 가볍게 살아보고, 대중교통 중심 적응도 보고 싶은 탐색형 사용자",
				RecommendationSurveyAnswers.ClimatePreference.MILD,
				RecommendationSurveyAnswers.SeasonTolerance.MEDIUM,
				RecommendationSurveyAnswers.PacePreference.RELAXED,
				RecommendationSurveyAnswers.CostQualityPreference.VALUE_FIRST,
				RecommendationSurveyAnswers.EnvironmentPreference.NATURE,
				RecommendationSurveyAnswers.EnglishSupportNeed.MEDIUM,
				PrimaryFocus.SAFETY,
				RecommendationSurveyAnswers.SettlementPreference.EXPERIENCE,
				RecommendationSurveyAnswers.MobilityPreference.TRANSIT_FIRST,
				List.of("뉴질랜드", "말레이시아", "우루과이"), "3~4점"),
			scenario("P16", "같은 저예산 자연형이지만 장기 정착과 넓은 생활 공간을 더 중시하는 안정형 사용자",
				RecommendationSurveyAnswers.ClimatePreference.MILD,
				RecommendationSurveyAnswers.SeasonTolerance.MEDIUM,
				RecommendationSurveyAnswers.PacePreference.RELAXED,
				RecommendationSurveyAnswers.CostQualityPreference.VALUE_FIRST,
				RecommendationSurveyAnswers.EnvironmentPreference.NATURE,
				RecommendationSurveyAnswers.EnglishSupportNeed.MEDIUM,
				PrimaryFocus.SAFETY,
				RecommendationSurveyAnswers.SettlementPreference.STABILITY,
				RecommendationSurveyAnswers.MobilityPreference.SPACE_FIRST,
				List.of("뉴질랜드", "우루과이", "포르투갈"), "4~5점"),
			scenario("P17", "빠른 고도시와 다문화 경험을 우선하고, 정착보다 먼저 부딪혀 보고 싶은 도심 탐험형 사용자",
				RecommendationSurveyAnswers.ClimatePreference.WARM,
				RecommendationSurveyAnswers.SeasonTolerance.HIGH,
				RecommendationSurveyAnswers.PacePreference.FAST,
				RecommendationSurveyAnswers.CostQualityPreference.BALANCED,
				RecommendationSurveyAnswers.EnvironmentPreference.CITY,
				RecommendationSurveyAnswers.EnglishSupportNeed.MEDIUM,
				PrimaryFocus.DIVERSITY,
				RecommendationSurveyAnswers.SettlementPreference.EXPERIENCE,
				RecommendationSurveyAnswers.MobilityPreference.TRANSIT_FIRST,
				List.of("싱가포르", "아랍에미리트", "브라질"), "4~5점"),
			scenario("P18", "같은 고도시 성향이어도 장기 정착 안정성과 과밀 적응 가능성을 같이 보는 현실형 사용자",
				RecommendationSurveyAnswers.ClimatePreference.WARM,
				RecommendationSurveyAnswers.SeasonTolerance.HIGH,
				RecommendationSurveyAnswers.PacePreference.FAST,
				RecommendationSurveyAnswers.CostQualityPreference.BALANCED,
				RecommendationSurveyAnswers.EnvironmentPreference.CITY,
				RecommendationSurveyAnswers.EnglishSupportNeed.MEDIUM,
				PrimaryFocus.DIVERSITY,
				RecommendationSurveyAnswers.SettlementPreference.STABILITY,
				RecommendationSurveyAnswers.MobilityPreference.SPACE_FIRST,
				List.of("싱가포르", "아랍에미리트", "대한민국"), "3~4점")
		);
	}

	private static RecommendationOfflinePersonaScenario scenario(
		String id,
		String description,
		RecommendationSurveyAnswers.ClimatePreference climatePreference,
		RecommendationSurveyAnswers.SeasonTolerance seasonTolerance,
		RecommendationSurveyAnswers.PacePreference pacePreference,
		RecommendationSurveyAnswers.CostQualityPreference costQualityPreference,
		RecommendationSurveyAnswers.EnvironmentPreference environmentPreference,
		RecommendationSurveyAnswers.EnglishSupportNeed englishSupportNeed,
		PrimaryFocus primaryFocus,
		RecommendationSurveyAnswers.SettlementPreference settlementPreference,
		RecommendationSurveyAnswers.MobilityPreference mobilityPreference,
		List<String> expectedCandidates,
		String expectedSatisfactionRange
	) {
		return new RecommendationOfflinePersonaScenario(
			id,
			description,
			new RecommendationSurveyAnswers(
				climatePreference,
				seasonTolerance,
				pacePreference,
				costQualityPreference,
				environmentPreference,
				englishSupportNeed,
				safetyImportance(primaryFocus),
				publicServiceImportance(primaryFocus),
				foodImportance(primaryFocus),
				diversityImportance(primaryFocus),
				settlementPreference,
				mobilityPreference
			),
			expectedCandidates,
			expectedSatisfactionRange
		);
	}

	private static RecommendationSurveyAnswers.ImportanceLevel safetyImportance(PrimaryFocus primaryFocus) {
		return switch (primaryFocus) {
			case SAFETY -> RecommendationSurveyAnswers.ImportanceLevel.HIGH;
			case PUBLIC_SERVICE -> RecommendationSurveyAnswers.ImportanceLevel.MEDIUM;
			case FOOD, DIVERSITY -> RecommendationSurveyAnswers.ImportanceLevel.LOW;
		};
	}

	private static RecommendationSurveyAnswers.ImportanceLevel publicServiceImportance(PrimaryFocus primaryFocus) {
		return switch (primaryFocus) {
			case PUBLIC_SERVICE -> RecommendationSurveyAnswers.ImportanceLevel.HIGH;
			case SAFETY -> RecommendationSurveyAnswers.ImportanceLevel.MEDIUM;
			case FOOD, DIVERSITY -> RecommendationSurveyAnswers.ImportanceLevel.LOW;
		};
	}

	private static RecommendationSurveyAnswers.ImportanceLevel foodImportance(PrimaryFocus primaryFocus) {
		return switch (primaryFocus) {
			case FOOD -> RecommendationSurveyAnswers.ImportanceLevel.HIGH;
			case SAFETY, PUBLIC_SERVICE, DIVERSITY -> RecommendationSurveyAnswers.ImportanceLevel.LOW;
		};
	}

	private static RecommendationSurveyAnswers.ImportanceLevel diversityImportance(PrimaryFocus primaryFocus) {
		return switch (primaryFocus) {
			case DIVERSITY -> RecommendationSurveyAnswers.ImportanceLevel.HIGH;
			case FOOD -> RecommendationSurveyAnswers.ImportanceLevel.MEDIUM;
			case SAFETY, PUBLIC_SERVICE -> RecommendationSurveyAnswers.ImportanceLevel.LOW;
		};
	}

	private enum PrimaryFocus {
		SAFETY,
		PUBLIC_SERVICE,
		FOOD,
		DIVERSITY
	}
}
