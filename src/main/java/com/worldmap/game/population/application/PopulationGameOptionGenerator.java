package com.worldmap.game.population.application;

import com.worldmap.country.domain.Country;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class PopulationGameOptionGenerator {

	public PopulationRoundOptions generate(Country targetCountry, List<Country> countries) {
		if (countries.size() < 4) {
			throw new IllegalStateException("인구수 보기형 문제를 만들기 위한 국가 수가 부족합니다.");
		}

		List<PopulationScaleBand> bands = PopulationScaleBandCatalog.bands();
		PopulationScaleBand correctBand = PopulationScaleBandCatalog.resolveByPopulation(targetCountry.getPopulation());
		int correctBandIndex = bands.indexOf(correctBand);
		int windowStart = Math.max(0, Math.min(correctBandIndex - 1, bands.size() - 4));
		List<Long> options = bands.subList(windowStart, windowStart + 4)
			.stream()
			.map(PopulationScaleBand::lowerBoundInclusive)
			.toList();

		return new PopulationRoundOptions(List.copyOf(options), correctBandIndex - windowStart + 1);
	}
}
