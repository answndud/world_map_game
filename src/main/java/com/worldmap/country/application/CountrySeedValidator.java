package com.worldmap.country.application;

import com.worldmap.country.infrastructure.CountrySeedReader.CountrySeedDocument;
import com.worldmap.country.infrastructure.CountrySeedReader.CountrySeedItem;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class CountrySeedValidator {

	public void validate(CountrySeedDocument document) {
		if (document == null) {
			throw new IllegalStateException("국가 시드 문서를 읽지 못했습니다.");
		}

		if (document.metadata() == null) {
			throw new IllegalStateException("국가 시드 메타데이터가 없습니다.");
		}

		if (document.metadata().populationYear() == null || document.metadata().populationYear() < 1900) {
			throw new IllegalStateException("populationYear 메타데이터가 올바르지 않습니다.");
		}

		if (document.countries() == null || document.countries().isEmpty()) {
			throw new IllegalStateException("국가 시드 목록이 비어 있습니다.");
		}

		Set<String> iso2Codes = new HashSet<>();
		Set<String> iso3Codes = new HashSet<>();

		for (CountrySeedItem item : document.countries()) {
			validateItem(item, iso2Codes, iso3Codes);
		}
	}

	private void validateItem(CountrySeedItem item, Set<String> iso2Codes, Set<String> iso3Codes) {
		requireText(item.iso2Code(), "iso2Code");
		requireText(item.iso3Code(), "iso3Code");
		requireText(item.nameKr(), "nameKr");
		requireText(item.nameEn(), "nameEn");
		requireText(item.capitalCity(), "capitalCity");

		if (item.iso2Code().length() != 2 || !item.iso2Code().equals(item.iso2Code().toUpperCase(Locale.ROOT))) {
			throw new IllegalStateException("iso2Code 형식이 올바르지 않습니다: " + item.iso2Code());
		}

		if (item.iso3Code().length() != 3 || !item.iso3Code().equals(item.iso3Code().toUpperCase(Locale.ROOT))) {
			throw new IllegalStateException("iso3Code 형식이 올바르지 않습니다: " + item.iso3Code());
		}

		if (!iso2Codes.add(item.iso2Code())) {
			throw new IllegalStateException("중복 iso2Code가 있습니다: " + item.iso2Code());
		}

		if (!iso3Codes.add(item.iso3Code())) {
			throw new IllegalStateException("중복 iso3Code가 있습니다: " + item.iso3Code());
		}

		if (item.continent() == null) {
			throw new IllegalStateException("continent가 비어 있습니다: " + item.iso3Code());
		}

		if (item.referenceType() == null) {
			throw new IllegalStateException("referenceType이 비어 있습니다: " + item.iso3Code());
		}

		requireRange(item.referenceLatitude(), BigDecimal.valueOf(-90), BigDecimal.valueOf(90), "referenceLatitude", item.iso3Code());
		requireRange(item.referenceLongitude(), BigDecimal.valueOf(-180), BigDecimal.valueOf(180), "referenceLongitude", item.iso3Code());

		if (item.population() == null || item.population() <= 0) {
			throw new IllegalStateException("population 값이 올바르지 않습니다: " + item.iso3Code());
		}
	}

	private void requireText(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalStateException(fieldName + " 값이 비어 있습니다.");
		}
	}

	private void requireRange(
		BigDecimal value,
		BigDecimal min,
		BigDecimal max,
		String fieldName,
		String iso3Code
	) {
		if (value == null || value.compareTo(min) < 0 || value.compareTo(max) > 0) {
			throw new IllegalStateException(fieldName + " 범위가 올바르지 않습니다: " + iso3Code);
		}
	}
}
