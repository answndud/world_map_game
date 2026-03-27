package com.worldmap.game.flag.application;

public record FlagAsset(
	String iso3Code,
	String relativePath,
	String format,
	String source,
	String licenseNote
) {
}
