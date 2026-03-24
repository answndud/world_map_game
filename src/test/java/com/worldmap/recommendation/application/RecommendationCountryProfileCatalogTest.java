package com.worldmap.recommendation.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class RecommendationCountryProfileCatalogTest {

	@Test
	void profilesExposeExpandedUniquePoolBackedBySeedCountries() throws IOException {
		RecommendationCountryProfileCatalog catalog = new RecommendationCountryProfileCatalog();
		Set<String> profileIsoCodes = catalog.profiles().stream()
			.map(RecommendationCountryProfile::iso3Code)
			.collect(Collectors.toSet());

		assertThat(catalog.profiles()).hasSize(30);
		assertThat(profileIsoCodes).hasSize(30);
		assertThat(profileIsoCodes)
			.contains("USA", "GBR", "FRA", "ARE", "THA", "BRA", "MEX", "ZAF");

		Set<String> seedIsoCodes = readSeedIsoCodes();
		assertThat(seedIsoCodes).containsAll(profileIsoCodes);
	}

	private Set<String> readSeedIsoCodes() throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		try (InputStream inputStream = RecommendationCountryProfileCatalogTest.class.getResourceAsStream("/data/countries.json")) {
			JsonNode root = objectMapper.readTree(inputStream);
			return root.path("countries").findValuesAsText("iso3Code").stream().collect(Collectors.toSet());
		}
	}
}
