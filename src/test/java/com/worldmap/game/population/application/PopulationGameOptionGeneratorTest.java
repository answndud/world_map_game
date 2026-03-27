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
	void optionsContainCorrectScaleBandAndFourUniqueChoices() {
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
		Long correctBandLowerBound = PopulationScaleBandCatalog.resolveByPopulation(target.getPopulation()).lowerBoundInclusive();

		assertThat(options.options()).hasSize(4);
		assertThat(options.options()).doesNotHaveDuplicates();
		assertThat(options.options()).contains(correctBandLowerBound);
		assertThat(options.correctOptionNumber()).isBetween(1, 4);
		assertThat(options.options().get(options.correctOptionNumber() - 1)).isEqualTo(correctBandLowerBound);
	}

	private Country country(String iso3Code, String nameKr, Long population) {
		return Country.create(
			iso3Code.substring(0, 2),
			iso3Code,
			nameKr,
			nameKr,
			Continent.ASIA,
			"Capital",
			"수도",
			BigDecimal.ZERO,
			BigDecimal.ZERO,
			CountryReferenceType.CAPITAL_CITY,
			population,
			2024
		);
	}
}
