package com.worldmap.country.application;

import com.worldmap.country.domain.Continent;

public record CountrySummaryView(
	String iso3Code,
	String nameKr,
	Continent continent,
	Long population,
	Integer populationYear
) {
}
