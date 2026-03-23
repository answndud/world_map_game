package com.worldmap.game.population.application;

import com.worldmap.country.domain.Country;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class PopulationGameOptionGenerator {

	public PopulationRoundOptions generate(Country targetCountry, List<Country> countries) {
		if (countries.size() < 4) {
			throw new IllegalStateException("인구수 보기형 문제를 만들기 위한 국가 수가 부족합니다.");
		}

		List<Country> sorted = countries.stream()
			.sorted(Comparator.comparing(Country::getPopulation))
			.toList();

		int targetIndex = findTargetIndex(sorted, targetCountry);
		Set<Long> distractors = new LinkedHashSet<>();

		for (int distance = 1; distance < sorted.size() && distractors.size() < 3; distance++) {
			int leftIndex = targetIndex - distance;
			int rightIndex = targetIndex + distance;

			if (leftIndex >= 0) {
				distractors.add(sorted.get(leftIndex).getPopulation());
			}

			if (rightIndex < sorted.size()) {
				distractors.add(sorted.get(rightIndex).getPopulation());
			}
		}

		if (distractors.size() < 3) {
			List<Long> fallback = sorted.stream()
				.map(Country::getPopulation)
				.filter(population -> !population.equals(targetCountry.getPopulation()))
				.distinct()
				.toList();

			for (Long population : fallback) {
				if (distractors.size() >= 3) {
					break;
				}
				distractors.add(population);
			}
		}

		List<Long> options = new ArrayList<>(distractors).subList(0, 3);
		options = new ArrayList<>(options);
		options.add(targetCountry.getPopulation());
		Collections.shuffle(options);

		return new PopulationRoundOptions(
			List.copyOf(options),
			options.indexOf(targetCountry.getPopulation()) + 1
		);
	}

	private int findTargetIndex(List<Country> countries, Country targetCountry) {
		for (int index = 0; index < countries.size(); index++) {
			if (countries.get(index).getIso3Code().equals(targetCountry.getIso3Code())) {
				return index;
			}
		}

		throw new IllegalArgumentException("대상 국가를 옵션 생성 목록에서 찾지 못했습니다.");
	}
}
