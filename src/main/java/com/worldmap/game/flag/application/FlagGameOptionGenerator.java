package com.worldmap.game.flag.application;

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

		collectDistinctCountryNames(
			availableCountries.stream()
				.filter(country -> !country.iso3Code().equals(targetCountry.iso3Code()))
				.filter(country -> country.continent() == targetCountry.continent())
				.toList(),
			distractors,
			usedCountryNameKeys,
			3
		);
		collectDistinctCountryNames(
			availableCountries.stream()
				.filter(country -> !country.iso3Code().equals(targetCountry.iso3Code()))
				.toList(),
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

	private String normalizeCountryName(String countryName) {
		return countryName == null
			? ""
			: countryName.trim().toLowerCase(Locale.ROOT);
	}
}
