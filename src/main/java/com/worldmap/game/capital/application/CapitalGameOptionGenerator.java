package com.worldmap.game.capital.application;

import com.worldmap.country.domain.Country;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Component;

@Component
public class CapitalGameOptionGenerator {

	public CapitalRoundOptions generate(Country targetCountry, List<Country> countries) {
		if (countries.size() < 4) {
			throw new IllegalStateException("수도 맞히기 문제를 만들기 위한 국가 수가 부족합니다.");
		}

		Set<String> usedCapitalKeys = new LinkedHashSet<>();
		usedCapitalKeys.add(normalizeCapital(targetCountry.getCapitalCity()));
		List<String> distractors = new ArrayList<>();

		collectDistinctCapitals(
			countries.stream()
				.filter(country -> !country.getIso3Code().equals(targetCountry.getIso3Code()))
				.filter(country -> country.getContinent() == targetCountry.getContinent())
				.toList(),
			distractors,
			usedCapitalKeys,
			3
		);
		collectDistinctCapitals(
			countries.stream()
				.filter(country -> !country.getIso3Code().equals(targetCountry.getIso3Code()))
				.toList(),
			distractors,
			usedCapitalKeys,
			3
		);

		if (distractors.size() < 3) {
			throw new IllegalStateException("수도 맞히기 보기를 구성할 수 없습니다.");
		}

		List<String> options = new ArrayList<>(distractors);
		int insertionIndex = ThreadLocalRandom.current().nextInt(4);
		options.add(insertionIndex, targetCountry.getCapitalCity());

		return new CapitalRoundOptions(List.copyOf(options), insertionIndex + 1);
	}

	private void collectDistinctCapitals(
		List<Country> countries,
		List<String> distractors,
		Set<String> usedCapitalKeys,
		int targetSize
	) {
		if (distractors.size() >= targetSize) {
			return;
		}

		List<Country> shuffled = new ArrayList<>(countries);
		Collections.shuffle(shuffled);

		for (Country country : shuffled) {
			if (distractors.size() >= targetSize) {
				return;
			}

			String capitalCity = country.getCapitalCity();
			if (capitalCity == null || capitalCity.isBlank()) {
				continue;
			}

			String normalized = normalizeCapital(capitalCity);
			if (!usedCapitalKeys.add(normalized)) {
				continue;
			}

			distractors.add(capitalCity.trim());
		}
	}

	private String normalizeCapital(String capitalCity) {
		return capitalCity == null
			? ""
			: capitalCity.trim().toLowerCase(Locale.ROOT);
	}
}
