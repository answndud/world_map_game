package com.worldmap.game.population.application;

import java.util.List;

public final class PopulationScaleBandCatalog {

	private static final List<PopulationScaleBand> BANDS = List.of(
		new PopulationScaleBand(0L, 10_000_000L, "1천만 미만"),
		new PopulationScaleBand(10_000_000L, 30_000_000L, "1천만 ~ 3천만"),
		new PopulationScaleBand(30_000_000L, 70_000_000L, "3천만 ~ 7천만"),
		new PopulationScaleBand(70_000_000L, 150_000_000L, "7천만 ~ 1억 5천만"),
		new PopulationScaleBand(150_000_000L, 300_000_000L, "1억 5천만 ~ 3억"),
		new PopulationScaleBand(300_000_000L, 600_000_000L, "3억 ~ 6억"),
		new PopulationScaleBand(600_000_000L, 1_000_000_000L, "6억 ~ 10억"),
		new PopulationScaleBand(1_000_000_000L, null, "10억 이상")
	);

	private PopulationScaleBandCatalog() {
	}

	public static List<PopulationScaleBand> bands() {
		return BANDS;
	}

	public static PopulationScaleBand resolveByPopulation(Long population) {
		return BANDS.stream()
			.filter(band -> band.contains(population))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("지원하지 않는 인구 규모입니다: " + population));
	}

	public static PopulationScaleBand resolveByLowerBound(Long lowerBoundInclusive) {
		return BANDS.stream()
			.filter(band -> band.lowerBoundInclusive().equals(lowerBoundInclusive))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("지원하지 않는 구간 시작값입니다: " + lowerBoundInclusive));
	}
}
