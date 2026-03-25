package com.worldmap.recommendation.application;

import com.worldmap.recommendation.domain.RecommendationSurveyAnswers;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RecommendationPersonaBaselineCatalog {

	public List<RecommendationPersonaBaselineScenario> scenarios() {
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
				List.of("싱가포르", "아랍에미리트", "미국"), "4~5점", "", false),
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
				List.of("말레이시아", "태국", "멕시코"), "4~5점", "", false),
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
				List.of("노르웨이", "핀란드", "스웨덴"), "4~5점", "", false),
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
				List.of("우루과이", "칠레", "스페인"), "3~4점",
				"복지형 시나리오는 기후와 비용만 맞는 후보보다 공공서비스와 정착 안정성이 함께 올라오는지 본다.", false),
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
				List.of("싱가포르", "아랍에미리트", "미국"), "4~5점", "", false),
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
				List.of("우루과이", "아일랜드", "캐나다"), "3~4점",
				"현실형 저예산 사용자는 물가뿐 아니라 영어 적응과 초기 정착 장벽이 낮은 후보가 유지되는지 본다.", false),
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
				List.of("일본", "대한민국", "싱가포르"), "4~5점", "", false),
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
				List.of("뉴질랜드", "캐나다", "스웨덴"), "4~5점", "", false),
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
				List.of("아랍에미리트", "싱가포르", "호주"), "4~5점", "", false),
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
				List.of("미국", "멕시코", "브라질"), "3~4점", "", false),
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
				List.of("캐나다", "덴마크", "네덜란드"), "4~5점", "", false),
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
				List.of("포르투갈", "우루과이", "뉴질랜드"), "3~4점", "", false),
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
				List.of("미국", "영국", "싱가포르"), "4~5점",
				"온화한 기후 선호가 도시성·영어 지원 점수에 눌리지 않는지 확인하는 고도시 다양성 시나리오다.", false),
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
				List.of("말레이시아", "태국", "베트남"), "4~5점", "", false),
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
				List.of("뉴질랜드", "말레이시아", "우루과이"), "3~4점",
				"`EXPERIENCE / TRANSIT_FIRST`가 들어오면 말레이시아가 top 3에 유지되는지 본다.", true),
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
				List.of("뉴질랜드", "우루과이", "포르투갈"), "4~5점",
				"`STABILITY / SPACE_FIRST`가 들어오면 포르투갈이 다시 상단 후보로 돌아오는지 본다.", true),
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
				List.of("싱가포르", "아랍에미리트", "브라질"), "4~5점",
				"active-signal 문항이 들어와도 경험형 후보인 브라질이 top 3에 남는지 확인한다.", true),
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
				List.of("싱가포르", "아랍에미리트", "대한민국"), "3~4점",
				"같은 기본 취향에서 `STABILITY / SPACE_FIRST`가 대한민국 같은 정착형 후보로 이동시키는지 본다.", true)
		);
	}

	private RecommendationPersonaBaselineScenario scenario(
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
		String expectedSatisfactionRange,
		String analysisNote,
		boolean activeSignal
	) {
		return new RecommendationPersonaBaselineScenario(
			id,
			description,
			new RecommendationSurveyAnswers(
				climatePreference,
				seasonStylePreference(climatePreference, environmentPreference),
				seasonTolerance,
				pacePreference,
				crowdPreference(pacePreference, environmentPreference),
				costQualityPreference,
				housingPreference(environmentPreference, mobilityPreference),
				environmentPreference,
				mobilityPreference,
				englishSupportNeed,
				newcomerSupportNeed(englishSupportNeed, primaryFocus),
				safetyImportance(primaryFocus),
				publicServiceImportance(primaryFocus),
				digitalImportance(pacePreference),
				foodImportance(primaryFocus),
				diversityImportance(primaryFocus),
				cultureImportance(primaryFocus, pacePreference),
				workLifePreference(pacePreference),
				settlementPreference,
				futureBasePreference(settlementPreference)
			),
			expectedCandidates,
			expectedSatisfactionRange,
			analysisNote,
			activeSignal
		);
	}

	private RecommendationSurveyAnswers.SeasonStylePreference seasonStylePreference(
		RecommendationSurveyAnswers.ClimatePreference climatePreference,
		RecommendationSurveyAnswers.EnvironmentPreference environmentPreference
	) {
		if (climatePreference == RecommendationSurveyAnswers.ClimatePreference.COLD) {
			return RecommendationSurveyAnswers.SeasonStylePreference.DISTINCT;
		}
		if (environmentPreference == RecommendationSurveyAnswers.EnvironmentPreference.CITY) {
			return RecommendationSurveyAnswers.SeasonStylePreference.STABLE;
		}
		return RecommendationSurveyAnswers.SeasonStylePreference.BALANCED;
	}

	private RecommendationSurveyAnswers.CrowdPreference crowdPreference(
		RecommendationSurveyAnswers.PacePreference pacePreference,
		RecommendationSurveyAnswers.EnvironmentPreference environmentPreference
	) {
		if (pacePreference == RecommendationSurveyAnswers.PacePreference.FAST
			&& environmentPreference == RecommendationSurveyAnswers.EnvironmentPreference.CITY) {
			return RecommendationSurveyAnswers.CrowdPreference.LIVELY;
		}
		if (pacePreference == RecommendationSurveyAnswers.PacePreference.RELAXED
			|| environmentPreference == RecommendationSurveyAnswers.EnvironmentPreference.NATURE) {
			return RecommendationSurveyAnswers.CrowdPreference.CALM;
		}
		return RecommendationSurveyAnswers.CrowdPreference.BALANCED;
	}

	private RecommendationSurveyAnswers.HousingPreference housingPreference(
		RecommendationSurveyAnswers.EnvironmentPreference environmentPreference,
		RecommendationSurveyAnswers.MobilityPreference mobilityPreference
	) {
		if (environmentPreference == RecommendationSurveyAnswers.EnvironmentPreference.CITY
			&& mobilityPreference == RecommendationSurveyAnswers.MobilityPreference.TRANSIT_FIRST) {
			return RecommendationSurveyAnswers.HousingPreference.CENTER_FIRST;
		}
		if (environmentPreference == RecommendationSurveyAnswers.EnvironmentPreference.NATURE
			|| mobilityPreference == RecommendationSurveyAnswers.MobilityPreference.SPACE_FIRST) {
			return RecommendationSurveyAnswers.HousingPreference.SPACE_FIRST;
		}
		return RecommendationSurveyAnswers.HousingPreference.BALANCED;
	}

	private RecommendationSurveyAnswers.NewcomerSupportNeed newcomerSupportNeed(
		RecommendationSurveyAnswers.EnglishSupportNeed englishSupportNeed,
		PrimaryFocus primaryFocus
	) {
		if (englishSupportNeed == RecommendationSurveyAnswers.EnglishSupportNeed.HIGH) {
			return RecommendationSurveyAnswers.NewcomerSupportNeed.HIGH;
		}
		if (primaryFocus == PrimaryFocus.DIVERSITY || primaryFocus == PrimaryFocus.PUBLIC_SERVICE) {
			return RecommendationSurveyAnswers.NewcomerSupportNeed.MEDIUM;
		}
		return RecommendationSurveyAnswers.NewcomerSupportNeed.LOW;
	}

	private RecommendationSurveyAnswers.ImportanceLevel safetyImportance(PrimaryFocus primaryFocus) {
		return switch (primaryFocus) {
			case SAFETY -> RecommendationSurveyAnswers.ImportanceLevel.HIGH;
			case PUBLIC_SERVICE -> RecommendationSurveyAnswers.ImportanceLevel.MEDIUM;
			case FOOD, DIVERSITY -> RecommendationSurveyAnswers.ImportanceLevel.LOW;
		};
	}

	private RecommendationSurveyAnswers.ImportanceLevel publicServiceImportance(PrimaryFocus primaryFocus) {
		return switch (primaryFocus) {
			case PUBLIC_SERVICE -> RecommendationSurveyAnswers.ImportanceLevel.HIGH;
			case SAFETY -> RecommendationSurveyAnswers.ImportanceLevel.MEDIUM;
			case FOOD, DIVERSITY -> RecommendationSurveyAnswers.ImportanceLevel.LOW;
		};
	}

	private RecommendationSurveyAnswers.ImportanceLevel foodImportance(PrimaryFocus primaryFocus) {
		return switch (primaryFocus) {
			case FOOD -> RecommendationSurveyAnswers.ImportanceLevel.HIGH;
			case SAFETY, PUBLIC_SERVICE, DIVERSITY -> RecommendationSurveyAnswers.ImportanceLevel.LOW;
		};
	}

	private RecommendationSurveyAnswers.ImportanceLevel diversityImportance(PrimaryFocus primaryFocus) {
		return switch (primaryFocus) {
			case DIVERSITY -> RecommendationSurveyAnswers.ImportanceLevel.HIGH;
			case FOOD -> RecommendationSurveyAnswers.ImportanceLevel.MEDIUM;
			case SAFETY, PUBLIC_SERVICE -> RecommendationSurveyAnswers.ImportanceLevel.LOW;
		};
	}

	private RecommendationSurveyAnswers.ImportanceLevel digitalImportance(
		RecommendationSurveyAnswers.PacePreference pacePreference
	) {
		return switch (pacePreference) {
			case FAST -> RecommendationSurveyAnswers.ImportanceLevel.HIGH;
			case BALANCED -> RecommendationSurveyAnswers.ImportanceLevel.MEDIUM;
			case RELAXED -> RecommendationSurveyAnswers.ImportanceLevel.LOW;
		};
	}

	private RecommendationSurveyAnswers.ImportanceLevel cultureImportance(
		PrimaryFocus primaryFocus,
		RecommendationSurveyAnswers.PacePreference pacePreference
	) {
		if (primaryFocus == PrimaryFocus.DIVERSITY || primaryFocus == PrimaryFocus.FOOD) {
			return RecommendationSurveyAnswers.ImportanceLevel.HIGH;
		}
		if (pacePreference == RecommendationSurveyAnswers.PacePreference.RELAXED) {
			return RecommendationSurveyAnswers.ImportanceLevel.LOW;
		}
		return RecommendationSurveyAnswers.ImportanceLevel.MEDIUM;
	}

	private RecommendationSurveyAnswers.WorkLifePreference workLifePreference(
		RecommendationSurveyAnswers.PacePreference pacePreference
	) {
		return switch (pacePreference) {
			case FAST -> RecommendationSurveyAnswers.WorkLifePreference.DRIVE_FIRST;
			case BALANCED -> RecommendationSurveyAnswers.WorkLifePreference.BALANCED;
			case RELAXED -> RecommendationSurveyAnswers.WorkLifePreference.LIFE_FIRST;
		};
	}

	private RecommendationSurveyAnswers.FutureBasePreference futureBasePreference(
		RecommendationSurveyAnswers.SettlementPreference settlementPreference
	) {
		return switch (settlementPreference) {
			case EXPERIENCE -> RecommendationSurveyAnswers.FutureBasePreference.LIGHT_START;
			case BALANCED -> RecommendationSurveyAnswers.FutureBasePreference.BALANCED;
			case STABILITY -> RecommendationSurveyAnswers.FutureBasePreference.STABLE_BASE;
		};
	}

	private enum PrimaryFocus {
		SAFETY,
		PUBLIC_SERVICE,
		FOOD,
		DIVERSITY
	}
}
