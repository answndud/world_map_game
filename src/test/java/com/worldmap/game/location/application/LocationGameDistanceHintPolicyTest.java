package com.worldmap.game.location.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.worldmap.country.domain.Continent;
import com.worldmap.country.domain.Country;
import com.worldmap.country.domain.CountryReferenceType;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class LocationGameDistanceHintPolicyTest {

	private final LocationGameDistanceHintPolicy locationGameDistanceHintPolicy = new LocationGameDistanceHintPolicy();

	@Test
	void buildHintReturnsDistanceAndEightWayDirection() {
		Country selectedCountry = Country.create(
			"AR",
			"ARG",
			"아르헨티나",
			"Argentina",
			Continent.SOUTH_AMERICA,
			"Buenos Aires",
			BigDecimal.valueOf(-34.6037),
			BigDecimal.valueOf(-58.3816),
			CountryReferenceType.CAPITAL_CITY,
			46000000L,
			2024
		);
		Country targetCountry = Country.create(
			"BR",
			"BRA",
			"브라질",
			"Brazil",
			Continent.SOUTH_AMERICA,
			"Brasilia",
			BigDecimal.valueOf(-15.7939),
			BigDecimal.valueOf(-47.8828),
			CountryReferenceType.CAPITAL_CITY,
			211000000L,
			2024
		);

		LocationGameDistanceHint hint = locationGameDistanceHintPolicy.buildHint(selectedCountry, targetCountry);

		assertThat(hint.distanceKm()).isPositive();
		assertThat(hint.directionHint()).isEqualTo("북동쪽");
	}
}
