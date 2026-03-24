package com.worldmap.recommendation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.worldmap.country.domain.Continent;
import com.worldmap.country.domain.Country;
import com.worldmap.country.domain.CountryReferenceType;
import com.worldmap.country.domain.CountryRepository;
import com.worldmap.recommendation.domain.RecommendationSurveyAnswers;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class RecommendationSurveyServiceTest {

	@Test
	void recommendReturnsDeterministicTopThree() {
		CountryRepository countryRepository = mockCountryRepository(List.of(
			country("CAN", "캐나다", "Canada", Continent.NORTH_AMERICA, "오타와", 40_000_000L),
			country("AUS", "호주", "Australia", Continent.OCEANIA, "캔버라", 27_000_000L),
			country("NZL", "뉴질랜드", "New Zealand", Continent.OCEANIA, "웰링턴", 5_000_000L),
			country("SGP", "싱가포르", "Singapore", Continent.ASIA, "싱가포르", 5_900_000L),
			country("JPN", "일본", "Japan", Continent.ASIA, "도쿄", 124_000_000L),
			country("DEU", "독일", "Germany", Continent.EUROPE, "베를린", 84_000_000L),
			country("SWE", "스웨덴", "Sweden", Continent.EUROPE, "스톡홀름", 10_000_000L),
			country("ESP", "스페인", "Spain", Continent.EUROPE, "마드리드", 48_000_000L),
			country("PRT", "포르투갈", "Portugal", Continent.EUROPE, "리스본", 10_000_000L),
			country("CHE", "스위스", "Switzerland", Continent.EUROPE, "베른", 9_000_000L),
			country("NLD", "네덜란드", "Netherlands", Continent.EUROPE, "암스테르담", 18_000_000L),
			country("KOR", "대한민국", "South Korea", Continent.ASIA, "서울", 51_000_000L)
		));

		RecommendationSurveyService service = new RecommendationSurveyService(
			new RecommendationCountryProfileCatalog(),
			new RecommendationQuestionCatalog(),
			countryRepository
		);

		RecommendationSurveyResultView result = service.recommend(new RecommendationSurveyAnswers(
			RecommendationSurveyAnswers.ClimatePreference.WARM,
			RecommendationSurveyAnswers.SeasonStylePreference.STABLE,
			RecommendationSurveyAnswers.SeasonTolerance.HIGH,
			RecommendationSurveyAnswers.PacePreference.FAST,
			RecommendationSurveyAnswers.CrowdPreference.LIVELY,
			RecommendationSurveyAnswers.CostQualityPreference.QUALITY_FIRST,
			RecommendationSurveyAnswers.HousingPreference.CENTER_FIRST,
			RecommendationSurveyAnswers.EnvironmentPreference.CITY,
			RecommendationSurveyAnswers.MobilityPreference.TRANSIT_FIRST,
			RecommendationSurveyAnswers.EnglishSupportNeed.HIGH,
			RecommendationSurveyAnswers.NewcomerSupportNeed.MEDIUM,
			RecommendationSurveyAnswers.ImportanceLevel.MEDIUM,
			RecommendationSurveyAnswers.ImportanceLevel.MEDIUM,
			RecommendationSurveyAnswers.ImportanceLevel.HIGH,
			RecommendationSurveyAnswers.ImportanceLevel.MEDIUM,
			RecommendationSurveyAnswers.ImportanceLevel.HIGH,
			RecommendationSurveyAnswers.ImportanceLevel.HIGH,
			RecommendationSurveyAnswers.WorkLifePreference.DRIVE_FIRST,
			RecommendationSurveyAnswers.SettlementPreference.BALANCED,
			RecommendationSurveyAnswers.FutureBasePreference.BALANCED
		));

		assertThat(result.recommendations()).hasSize(3);
		assertThat(result.recommendations().getFirst().countryNameKr()).isEqualTo("싱가포르");
		assertThat(result.submittedPreferences()).hasSize(20);
	}

	@Test
	void recommendCanSurfaceExpandedPoolCandidates() {
		CountryRepository countryRepository = mockCountryRepository(List.of(
			country("SGP", "싱가포르", "Singapore", Continent.ASIA, "싱가포르", 5_900_000L),
			country("THA", "태국", "Thailand", Continent.ASIA, "방콕", 71_000_000L),
			country("MYS", "말레이시아", "Malaysia", Continent.ASIA, "쿠알라룸푸르", 35_000_000L),
			country("VNM", "베트남", "Vietnam", Continent.ASIA, "하노이", 101_000_000L),
			country("BRA", "브라질", "Brazil", Continent.SOUTH_AMERICA, "브라질리아", 211_000_000L),
			country("MEX", "멕시코", "Mexico", Continent.NORTH_AMERICA, "멕시코시티", 130_000_000L),
			country("ZAF", "남아프리카공화국", "South Africa", Continent.AFRICA, "프리토리아", 63_000_000L),
			country("PRT", "포르투갈", "Portugal", Continent.EUROPE, "리스본", 10_000_000L),
			country("ESP", "스페인", "Spain", Continent.EUROPE, "마드리드", 48_000_000L)
		));

		RecommendationSurveyService service = new RecommendationSurveyService(
			new RecommendationCountryProfileCatalog(),
			new RecommendationQuestionCatalog(),
			countryRepository
		);

		RecommendationSurveyResultView result = service.recommend(new RecommendationSurveyAnswers(
			RecommendationSurveyAnswers.ClimatePreference.WARM,
			RecommendationSurveyAnswers.SeasonStylePreference.STABLE,
			RecommendationSurveyAnswers.SeasonTolerance.MEDIUM,
			RecommendationSurveyAnswers.PacePreference.BALANCED,
			RecommendationSurveyAnswers.CrowdPreference.BALANCED,
			RecommendationSurveyAnswers.CostQualityPreference.VALUE_FIRST,
			RecommendationSurveyAnswers.HousingPreference.SPACE_FIRST,
			RecommendationSurveyAnswers.EnvironmentPreference.MIXED,
			RecommendationSurveyAnswers.MobilityPreference.BALANCED,
			RecommendationSurveyAnswers.EnglishSupportNeed.MEDIUM,
			RecommendationSurveyAnswers.NewcomerSupportNeed.HIGH,
			RecommendationSurveyAnswers.ImportanceLevel.LOW,
			RecommendationSurveyAnswers.ImportanceLevel.LOW,
			RecommendationSurveyAnswers.ImportanceLevel.MEDIUM,
			RecommendationSurveyAnswers.ImportanceLevel.HIGH,
			RecommendationSurveyAnswers.ImportanceLevel.MEDIUM,
			RecommendationSurveyAnswers.ImportanceLevel.MEDIUM,
			RecommendationSurveyAnswers.WorkLifePreference.BALANCED,
			RecommendationSurveyAnswers.SettlementPreference.BALANCED,
			RecommendationSurveyAnswers.FutureBasePreference.BALANCED
		));

		assertThat(result.recommendations()).hasSize(3);
		assertThat(result.recommendations().getFirst().countryNameKr()).isEqualTo("태국");
		assertThat(result.recommendations().getFirst().countryNameKr()).isNotEqualTo("싱가포르");
	}

	@Test
	void recommendCanReflectSettlementHousingAndFutureBaseQuestions() {
		CountryRepository countryRepository = mockCountryRepository(List.of(
			country("CAN", "캐나다", "Canada", Continent.NORTH_AMERICA, "오타와", 40_000_000L),
			country("NZL", "뉴질랜드", "New Zealand", Continent.OCEANIA, "웰링턴", 5_000_000L),
			country("SGP", "싱가포르", "Singapore", Continent.ASIA, "싱가포르", 5_900_000L),
			country("GBR", "영국", "United Kingdom", Continent.EUROPE, "런던", 68_000_000L)
		));

		RecommendationSurveyService service = new RecommendationSurveyService(
			new RecommendationCountryProfileCatalog(),
			new RecommendationQuestionCatalog(),
			countryRepository
		);

		RecommendationSurveyResultView result = service.recommend(new RecommendationSurveyAnswers(
			RecommendationSurveyAnswers.ClimatePreference.MILD,
			RecommendationSurveyAnswers.SeasonStylePreference.DISTINCT,
			RecommendationSurveyAnswers.SeasonTolerance.LOW,
			RecommendationSurveyAnswers.PacePreference.BALANCED,
			RecommendationSurveyAnswers.CrowdPreference.CALM,
			RecommendationSurveyAnswers.CostQualityPreference.QUALITY_FIRST,
			RecommendationSurveyAnswers.HousingPreference.SPACE_FIRST,
			RecommendationSurveyAnswers.EnvironmentPreference.MIXED,
			RecommendationSurveyAnswers.MobilityPreference.SPACE_FIRST,
			RecommendationSurveyAnswers.EnglishSupportNeed.HIGH,
			RecommendationSurveyAnswers.NewcomerSupportNeed.HIGH,
			RecommendationSurveyAnswers.ImportanceLevel.HIGH,
			RecommendationSurveyAnswers.ImportanceLevel.HIGH,
			RecommendationSurveyAnswers.ImportanceLevel.MEDIUM,
			RecommendationSurveyAnswers.ImportanceLevel.LOW,
			RecommendationSurveyAnswers.ImportanceLevel.MEDIUM,
			RecommendationSurveyAnswers.ImportanceLevel.LOW,
			RecommendationSurveyAnswers.WorkLifePreference.LIFE_FIRST,
			RecommendationSurveyAnswers.SettlementPreference.STABILITY,
			RecommendationSurveyAnswers.FutureBasePreference.STABLE_BASE
		));

		assertThat(result.recommendations()).hasSize(3);
		assertThat(result.recommendations().getFirst().countryNameKr()).isEqualTo("캐나다");
	}

	private CountryRepository mockCountryRepository(List<Country> countries) {
		CountryRepository countryRepository = mock(CountryRepository.class);
		when(countryRepository.findAllByOrderByNameKrAsc()).thenReturn(countries);
		return countryRepository;
	}

	private Country country(
		String iso3Code,
		String nameKr,
		String nameEn,
		Continent continent,
		String capitalCity,
		Long population
	) {
		return Country.create(
			iso3Code.substring(0, 2),
			iso3Code,
			nameKr,
			nameEn,
			continent,
			capitalCity,
			BigDecimal.ZERO,
			BigDecimal.ZERO,
			CountryReferenceType.CAPITAL_CITY,
			population,
			2024
		);
	}
}
