package com.worldmap.country.application;

import com.worldmap.country.domain.Continent;
import com.worldmap.country.domain.CountryReferenceType;
import java.math.BigDecimal;

public record CountryDetailView(
	String iso2Code,
	String iso3Code,
	String nameKr,
	String nameEn,
	Continent continent,
	String capitalCity,
	BigDecimal referenceLatitude,
	BigDecimal referenceLongitude,
	CountryReferenceType referenceType,
	Long population,
	Integer populationYear
) {
}
