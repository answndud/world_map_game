package com.worldmap.game.flag.application;

import com.worldmap.country.domain.Continent;

public record FlagQuestionCountryView(
	Long countryId,
	String iso3Code,
	String countryNameKr,
	String countryNameEn,
	Continent continent,
	String flagRelativePath,
	String flagFormat
) {
}
