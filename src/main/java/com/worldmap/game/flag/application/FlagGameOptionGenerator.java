package com.worldmap.game.flag.application;

import com.worldmap.country.domain.Continent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Component;

@Component
public class FlagGameOptionGenerator {

	public FlagRoundOptions generate(
		FlagQuestionCountryView targetCountry,
		List<FlagQuestionCountryView> availableCountries
	) {
		if (availableCountries.size() < 4) {
			throw new IllegalStateException("국기 게임 문제를 만들기 위한 국가 수가 부족합니다.");
		}

		Set<String> usedCountryNameKeys = new LinkedHashSet<>();
		usedCountryNameKeys.add(normalizeCountryName(targetCountry.countryNameKr()));
		List<String> distractors = new ArrayList<>();
		List<FlagQuestionCountryView> remainingCountries = availableCountries.stream()
			.filter(country -> !country.iso3Code().equals(targetCountry.iso3Code()))
			.toList();

		collectDistinctCountryNames(
			remainingCountries.stream()
				.filter(country -> country.continent() == targetCountry.continent())
				.toList(),
			distractors,
			usedCountryNameKeys,
			3
		);

		for (Continent fallbackContinent : fallbackContinents(targetCountry.continent())) {
			collectDistinctCountryNames(
				remainingCountries.stream()
					.filter(country -> country.continent() == fallbackContinent)
					.toList(),
				distractors,
				usedCountryNameKeys,
				3
			);
		}

		collectDistinctCountryNames(
			remainingCountries,
			distractors,
			usedCountryNameKeys,
			3
		);

		if (distractors.size() < 3) {
			throw new IllegalStateException("국기 게임 보기를 구성할 수 없습니다.");
		}

		List<String> options = new ArrayList<>(distractors);
		int insertionIndex = ThreadLocalRandom.current().nextInt(4);
		options.add(insertionIndex, targetCountry.countryNameKr());

		return new FlagRoundOptions(List.copyOf(options), insertionIndex + 1);
	}

	private void collectDistinctCountryNames(
		List<FlagQuestionCountryView> countries,
		List<String> distractors,
		Set<String> usedCountryNameKeys,
		int targetSize
	) {
		if (distractors.size() >= targetSize) {
			return;
		}

		List<FlagQuestionCountryView> shuffled = new ArrayList<>(countries);
		Collections.shuffle(shuffled);

		for (FlagQuestionCountryView country : shuffled) {
			if (distractors.size() >= targetSize) {
				return;
			}

			String countryName = country.countryNameKr();
			if (countryName == null || countryName.isBlank()) {
				continue;
			}

			String normalized = normalizeCountryName(countryName);
			if (!usedCountryNameKeys.add(normalized)) {
				continue;
			}

			distractors.add(countryName.trim());
		}
	}

	// When same-continent flags are insufficient, expand in a predictable regional order
	// before falling back to the full pool. This keeps distractors closer to the target.
	private List<Continent> fallbackContinents(Continent continent) {
		if (continent == null) {
			return List.of();
		}

		return switch (continent) {
			case AFRICA -> List.of(Continent.EUROPE, Continent.ASIA, Continent.SOUTH_AMERICA, Continent.NORTH_AMERICA, Continent.OCEANIA);
			case ASIA -> List.of(Continent.OCEANIA, Continent.EUROPE, Continent.AFRICA, Continent.NORTH_AMERICA, Continent.SOUTH_AMERICA);
			case EUROPE -> List.of(Continent.AFRICA, Continent.ASIA, Continent.NORTH_AMERICA, Continent.SOUTH_AMERICA, Continent.OCEANIA);
			case NORTH_AMERICA -> List.of(Continent.SOUTH_AMERICA, Continent.EUROPE, Continent.ASIA, Continent.OCEANIA, Continent.AFRICA);
			case OCEANIA -> List.of(Continent.ASIA, Continent.NORTH_AMERICA, Continent.EUROPE, Continent.SOUTH_AMERICA, Continent.AFRICA);
			case SOUTH_AMERICA -> List.of(Continent.NORTH_AMERICA, Continent.EUROPE, Continent.AFRICA, Continent.ASIA, Continent.OCEANIA);
		};
	}

	private String normalizeCountryName(String countryName) {
		return countryName == null
			? ""
			: countryName.trim().toLowerCase(Locale.ROOT);
	}
}
