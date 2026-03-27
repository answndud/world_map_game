package com.worldmap.game.flag.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.worldmap.country.domain.Continent;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class FlagQuestionCountryPoolServiceIntegrationTest {

	@Autowired
	private FlagQuestionCountryPoolService flagQuestionCountryPoolService;

	@Test
	void loadPoolReturnsOnlyCountriesBackedBySeedAndFlagAssets() {
		FlagQuestionCountryPoolView pool = flagQuestionCountryPoolService.loadPool();

		assertThat(pool.availableCountryCount()).isEqualTo(12);
		assertThat(pool.countries())
			.extracting(FlagQuestionCountryView::iso3Code)
			.containsExactlyInAnyOrder("AUT", "BEL", "DEU", "EST", "FRA", "IRL", "ITA", "JPN", "LTU", "NLD", "POL", "UKR");
		assertThat(pool.countries())
			.allSatisfy(country -> {
				assertThat(country.flagRelativePath()).startsWith("/images/flags/");
				assertThat(country.flagFormat()).isEqualTo("svg");
				assertThat(country.countryNameKr()).isNotBlank();
			});

		Map<Continent, Integer> continentCountMap = pool.continentCounts().stream()
			.collect(Collectors.toMap(FlagQuestionCountryContinentCountView::continent, FlagQuestionCountryContinentCountView::countryCount));
		assertThat(continentCountMap).containsEntry(Continent.EUROPE, 11);
		assertThat(continentCountMap).containsEntry(Continent.ASIA, 1);
	}

	@Test
	void serviceSupportsIso3LookupAndContinentFiltering() {
		FlagQuestionCountryView japan = flagQuestionCountryPoolService.findAvailableCountry("jpn")
			.orElseThrow();

		assertThat(japan.countryNameKr()).isEqualTo("일본");
		assertThat(japan.continent()).isEqualTo(Continent.ASIA);
		assertThat(flagQuestionCountryPoolService.availableCountriesByContinent(Continent.ASIA))
			.extracting(FlagQuestionCountryView::iso3Code)
			.containsExactly("JPN");
		assertThat(flagQuestionCountryPoolService.findAvailableCountry("KOR")).isEmpty();
	}
}
