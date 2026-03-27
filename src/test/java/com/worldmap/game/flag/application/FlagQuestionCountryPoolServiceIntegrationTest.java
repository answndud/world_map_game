package com.worldmap.game.flag.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.worldmap.country.domain.Continent;
import java.util.Map;
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

		assertThat(pool.availableCountryCount()).isEqualTo(36);
		assertThat(pool.countries())
			.extracting(FlagQuestionCountryView::iso3Code)
			.containsExactlyInAnyOrder(
				"ARG", "AUS", "AUT", "BEL", "BRA", "CAN", "CHE", "CHL", "CHN", "COL",
				"DEU", "EGY", "ESP", "EST", "FRA", "GBR", "IDN", "IND", "IRL", "ITA",
				"JPN", "KEN", "KOR", "LTU", "MAR", "MEX", "NLD", "NZL", "POL", "PRT",
				"SGP", "THA", "TUR", "UKR", "USA", "ZAF"
			);
		assertThat(pool.countries())
			.allSatisfy(country -> {
				assertThat(country.flagRelativePath()).startsWith("/images/flags/");
				assertThat(country.flagFormat()).isEqualTo("svg");
				assertThat(country.countryNameKr()).isNotBlank();
			});

		Map<Continent, Integer> continentCountMap = pool.continentCounts().stream()
			.collect(Collectors.toMap(FlagQuestionCountryContinentCountView::continent, FlagQuestionCountryContinentCountView::countryCount));
		assertThat(continentCountMap).containsEntry(Continent.EUROPE, 15);
		assertThat(continentCountMap).containsEntry(Continent.ASIA, 8);
		assertThat(continentCountMap).containsEntry(Continent.NORTH_AMERICA, 3);
		assertThat(continentCountMap).containsEntry(Continent.SOUTH_AMERICA, 4);
		assertThat(continentCountMap).containsEntry(Continent.AFRICA, 4);
		assertThat(continentCountMap).containsEntry(Continent.OCEANIA, 2);
	}

	@Test
	void serviceSupportsIso3LookupAndContinentFiltering() {
		FlagQuestionCountryView japan = flagQuestionCountryPoolService.findAvailableCountry("jpn")
			.orElseThrow();
		FlagQuestionCountryView korea = flagQuestionCountryPoolService.findAvailableCountry("KOR")
			.orElseThrow();

		assertThat(japan.countryNameKr()).isEqualTo("일본");
		assertThat(japan.continent()).isEqualTo(Continent.ASIA);
		assertThat(korea.countryNameKr()).isEqualTo("대한민국");
		assertThat(flagQuestionCountryPoolService.availableCountriesByContinent(Continent.ASIA))
			.extracting(FlagQuestionCountryView::iso3Code)
			.containsExactlyInAnyOrder("CHN", "IDN", "IND", "JPN", "KOR", "SGP", "THA", "TUR");
		assertThat(flagQuestionCountryPoolService.availableCountriesByContinent(Continent.OCEANIA))
			.extracting(FlagQuestionCountryView::iso3Code)
			.containsExactlyInAnyOrder("AUS", "NZL");
	}
}
