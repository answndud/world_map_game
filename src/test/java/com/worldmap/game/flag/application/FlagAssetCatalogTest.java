package com.worldmap.game.flag.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

import static org.assertj.core.api.Assertions.assertThat;

class FlagAssetCatalogTest {

	@Test
	void catalogLoadsStaticFlagAssetsAndSupportsIso3Lookup() {
		FlagAssetCatalog catalog = new FlagAssetCatalog(new ObjectMapper(), new DefaultResourceLoader());

		assertThat(catalog.assets()).hasSize(12);
		assertThat(catalog.supports("jpn")).isTrue();
		assertThat(catalog.findByIso3Code("JPN"))
			.get()
			.extracting(FlagAsset::relativePath, FlagAsset::format)
			.containsExactly("/images/flags/jpn.svg", "svg");
	}

	@Test
	void allManifestIso3CodesExistInCountrySeed() throws Exception {
		FlagAssetCatalog catalog = new FlagAssetCatalog(new ObjectMapper(), new DefaultResourceLoader());
		Set<String> assetIso3Codes = catalog.supportedIso3Codes();

		ObjectMapper objectMapper = new ObjectMapper();
		try (InputStream inputStream = FlagAssetCatalogTest.class.getResourceAsStream("/data/countries.json")) {
			JsonNode root = objectMapper.readTree(inputStream);
			Set<String> seededIso3Codes = iterable(root.get("countries"))
				.map(country -> country.get("iso3Code").asText())
				.collect(Collectors.toSet());

			assertThat(seededIso3Codes).containsAll(assetIso3Codes);
		}
	}

	private java.util.stream.Stream<JsonNode> iterable(JsonNode node) {
		return java.util.stream.StreamSupport.stream(node.spliterator(), false);
	}
}
