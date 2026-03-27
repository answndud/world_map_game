package com.worldmap.game.flag.application;

import com.worldmap.country.domain.Continent;
import com.worldmap.country.domain.Country;
import com.worldmap.country.domain.CountryRepository;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class FlagQuestionCountryPoolService {

	private final CountryRepository countryRepository;
	private final FlagAssetCatalog flagAssetCatalog;

	public FlagQuestionCountryPoolService(CountryRepository countryRepository, FlagAssetCatalog flagAssetCatalog) {
		this.countryRepository = countryRepository;
		this.flagAssetCatalog = flagAssetCatalog;
	}

	public FlagQuestionCountryPoolView loadPool() {
		List<Country> seededCountries = countryRepository.findAllByOrderByNameKrAsc();
		validateManifestAgainstSeed(seededCountries);

		List<FlagQuestionCountryView> countries = seededCountries.stream()
			.map(this::toAvailableCountry)
			.flatMap(Optional::stream)
			.toList();

		List<FlagQuestionCountryContinentCountView> continentCounts = countries.stream()
			.collect(Collectors.groupingBy(FlagQuestionCountryView::continent, Collectors.counting()))
			.entrySet()
			.stream()
			.sorted(Map.Entry.comparingByKey())
			.map(entry -> new FlagQuestionCountryContinentCountView(entry.getKey(), entry.getValue().intValue()))
			.toList();

		return new FlagQuestionCountryPoolView(countries.size(), continentCounts, countries);
	}

	public List<FlagQuestionCountryView> availableCountries() {
		return loadPool().countries();
	}

	public List<FlagQuestionCountryView> availableCountriesByContinent(Continent continent) {
		if (continent == null) {
			return List.of();
		}
		return availableCountries().stream()
			.filter(country -> country.continent() == continent)
			.toList();
	}

	public Optional<FlagQuestionCountryView> findAvailableCountry(String iso3Code) {
		if (iso3Code == null || iso3Code.isBlank()) {
			return Optional.empty();
		}
		String normalizedIso3Code = iso3Code.trim().toUpperCase(Locale.ROOT);
		return availableCountries().stream()
			.filter(country -> country.iso3Code().equals(normalizedIso3Code))
			.findFirst();
	}

	private void validateManifestAgainstSeed(List<Country> seededCountries) {
		Set<String> seededIso3Codes = seededCountries.stream()
			.map(Country::getIso3Code)
			.collect(Collectors.toCollection(LinkedHashSet::new));
		Set<String> unsupportedIso3Codes = new LinkedHashSet<>(flagAssetCatalog.supportedIso3Codes());
		unsupportedIso3Codes.removeAll(seededIso3Codes);

		if (!unsupportedIso3Codes.isEmpty()) {
			throw new IllegalStateException(
				"국기 자산 manifest에 있지만 country seed에 없는 ISO3 코드가 있습니다: " + unsupportedIso3Codes
			);
		}
	}

	private Optional<FlagQuestionCountryView> toAvailableCountry(Country country) {
		return flagAssetCatalog.findByIso3Code(country.getIso3Code())
			.map(asset -> new FlagQuestionCountryView(
				country.getId(),
				country.getIso3Code(),
				country.getNameKr(),
				country.getNameEn(),
				country.getContinent(),
				asset.relativePath(),
				asset.format()
			));
	}
}
