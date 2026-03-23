package com.worldmap.game.population.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.worldmap.country.domain.Continent;
import com.worldmap.country.domain.Country;
import com.worldmap.country.domain.CountryReferenceType;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class PopulationGameOptionGeneratorTest {

	private final PopulationGameOptionGenerator optionGenerator = new PopulationGameOptionGenerator();

	@Test
	void optionsContainCorrectPopulationAndFourUniqueChoices() {
		Country target = country("KOR", "대한민국", 51751065L);
		List<Country> countries = List.of(
			country("USA", "미국", 340110988L),
			country("JPN", "일본", 123975371L),
			target,
			country("MEX", "멕시코", 130861007L),
			country("CAN", "캐나다", 41288599L),
			country("AUS", "호주", 27196812L)
		);

		PopulationRoundOptions options = optionGenerator.generate(target, countries);

		assertThat(options.options()).hasSize(4);
		assertThat(options.options()).doesNotHaveDuplicates();
		assertThat(options.options()).contains(target.getPopulation());
		assertThat(options.correctOptionNumber()).isBetween(1, 4);
	}

	private Country country(String iso3Code, String nameKr, Long population) {
		return Country.create(
			iso3Code.substring(0, 2),
			iso3Code,
			nameKr,
			nameKr,
			Continent.ASIA,
			"Capital",
			BigDecimal.ZERO,
			BigDecimal.ZERO,
			CountryReferenceType.CAPITAL_CITY,
			population,
			2024
		);
	}
}
