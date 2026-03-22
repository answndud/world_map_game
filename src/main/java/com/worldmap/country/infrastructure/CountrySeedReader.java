package com.worldmap.country.infrastructure;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.worldmap.country.domain.Continent;
import com.worldmap.country.domain.CountryReferenceType;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public class CountrySeedReader {

	private final ObjectMapper objectMapper;
	private final ResourceLoader resourceLoader;

	public CountrySeedReader(ObjectMapper objectMapper, ResourceLoader resourceLoader) {
		this.objectMapper = objectMapper;
		this.resourceLoader = resourceLoader;
	}

	public CountrySeedDocument read(String location) {
		Resource resource = resourceLoader.getResource(location);

		if (!resource.exists()) {
			throw new IllegalStateException("국가 시드 파일을 찾을 수 없습니다: " + location);
		}

		try (InputStream inputStream = resource.getInputStream()) {
			return objectMapper.readValue(inputStream, CountrySeedDocument.class);
		} catch (IOException ex) {
			throw new IllegalStateException("국가 시드 파일을 읽는 중 오류가 발생했습니다: " + location, ex);
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record CountrySeedDocument(SeedMetadata metadata, List<CountrySeedItem> countries) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record SeedMetadata(
		String datasetVersion,
		String sourceName,
		String sourceUrl,
		String populationIndicator,
		Integer populationYear,
		String referenceNote
	) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record CountrySeedItem(
		String iso2Code,
		String iso3Code,
		String nameKr,
		String nameEn,
		Continent continent,
		String capitalCity,
		BigDecimal referenceLatitude,
		BigDecimal referenceLongitude,
		CountryReferenceType referenceType,
		Long population
	) {
	}
}
