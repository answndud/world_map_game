package com.worldmap.game.populationbattle.application;

import com.worldmap.country.domain.Country;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Component;

@Component
public class PopulationBattleGameOptionGenerator {

	public PopulationBattleRoundOptions generate(Country morePopulousCountry, Country lessPopulousCountry) {
		if (ThreadLocalRandom.current().nextBoolean()) {
			return new PopulationBattleRoundOptions(morePopulousCountry, lessPopulousCountry, 1);
		}

		return new PopulationBattleRoundOptions(lessPopulousCountry, morePopulousCountry, 2);
	}
}
