package com.worldmap.game.flag.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.worldmap.country.domain.Continent;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class FlagGameOptionGeneratorTest {

	private final FlagGameOptionGenerator optionGenerator = new FlagGameOptionGenerator();

	@Test
	void usesSameContinentDistractorsWhenThereAreEnoughCandidates() {
		FlagQuestionCountryView target = country("FRA", "프랑스", Continent.EUROPE);
		List<FlagQuestionCountryView> countries = List.of(
			target,
			country("DEU", "독일", Continent.EUROPE),
			country("ITA", "이탈리아", Continent.EUROPE),
			country("ESP", "스페인", Continent.EUROPE),
			country("JPN", "일본", Continent.ASIA),
			country("BRA", "브라질", Continent.SOUTH_AMERICA)
		);

		FlagRoundOptions options = optionGenerator.generate(target, countries);

		assertThat(distractors(options, target.countryNameKr()))
			.containsExactlyInAnyOrder("독일", "이탈리아", "스페인");
	}

	@Test
	void oceaniaFallsBackToAsiaBeforeOtherContinents() {
		FlagQuestionCountryView target = country("AUS", "호주", Continent.OCEANIA);
		List<FlagQuestionCountryView> countries = List.of(
			target,
			country("NZL", "뉴질랜드", Continent.OCEANIA),
			country("JPN", "일본", Continent.ASIA),
			country("KOR", "대한민국", Continent.ASIA),
			country("ESP", "스페인", Continent.EUROPE)
		);

		FlagRoundOptions options = optionGenerator.generate(target, countries);

		assertThat(distractors(options, target.countryNameKr()))
			.containsExactlyInAnyOrder("뉴질랜드", "일본", "대한민국")
			.doesNotContain("스페인");
	}

	@Test
	void northAmericaFallsBackToSouthAmericaBeforeEurope() {
		FlagQuestionCountryView target = country("USA", "미국", Continent.NORTH_AMERICA);
		List<FlagQuestionCountryView> countries = List.of(
			target,
			country("CAN", "캐나다", Continent.NORTH_AMERICA),
			country("MEX", "멕시코", Continent.NORTH_AMERICA),
			country("BRA", "브라질", Continent.SOUTH_AMERICA),
			country("ARG", "아르헨티나", Continent.SOUTH_AMERICA),
			country("ESP", "스페인", Continent.EUROPE)
		);

		FlagRoundOptions options = optionGenerator.generate(target, countries);
		Set<String> distractors = distractors(options, target.countryNameKr());

		assertThat(distractors).contains("캐나다", "멕시코");
		assertThat(distractors).anyMatch(country -> country.equals("브라질") || country.equals("아르헨티나"));
		assertThat(distractors).doesNotContain("스페인");
	}

	private Set<String> distractors(FlagRoundOptions options, String targetCountryName) {
		return options.options().stream()
			.filter(option -> !option.equals(targetCountryName))
			.collect(java.util.stream.Collectors.toSet());
	}

	private FlagQuestionCountryView country(String iso3Code, String countryNameKr, Continent continent) {
		return new FlagQuestionCountryView(
			1L,
			iso3Code,
			countryNameKr,
			countryNameKr,
			continent,
			"/images/flags/" + iso3Code.toLowerCase() + ".svg",
			"svg"
		);
	}
}
