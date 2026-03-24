package com.worldmap.country.application;

import com.worldmap.country.domain.Country;
import com.worldmap.country.domain.CountryRepository;
import com.worldmap.country.infrastructure.CountrySeedReader;
import com.worldmap.country.infrastructure.CountrySeedReader.CountrySeedDocument;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(10)
public class CountrySeedInitializer implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(CountrySeedInitializer.class);

	private final CountryRepository countryRepository;
	private final CountrySeedReader countrySeedReader;
	private final CountrySeedValidator countrySeedValidator;
	private final CountrySeedProperties countrySeedProperties;

	public CountrySeedInitializer(
		CountryRepository countryRepository,
		CountrySeedReader countrySeedReader,
		CountrySeedValidator countrySeedValidator,
		CountrySeedProperties countrySeedProperties
	) {
		this.countryRepository = countryRepository;
		this.countrySeedReader = countrySeedReader;
		this.countrySeedValidator = countrySeedValidator;
		this.countrySeedProperties = countrySeedProperties;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		if (!countrySeedProperties.isEnabled()) {
			log.info("Country seed loading is disabled.");
			return;
		}

		CountrySeedDocument document = countrySeedReader.read(countrySeedProperties.getLocation());
		countrySeedValidator.validate(document);

		Map<String, Country> existingCountriesByIso3 = countryRepository.findAll().stream()
			.collect(Collectors.toMap(
				Country::getIso3Code,
				Function.identity()
			));

		Set<String> seedIso3Codes = new HashSet<>();
		List<Country> countriesToPersist = new ArrayList<>();
		int insertedCount = 0;
		int updatedCount = 0;

		for (var item : document.countries()) {
			seedIso3Codes.add(item.iso3Code());

			Country existingCountry = existingCountriesByIso3.get(item.iso3Code());

			if (existingCountry == null) {
				countriesToPersist.add(Country.create(
					item.iso2Code(),
					item.iso3Code(),
					item.nameKr(),
					item.nameEn(),
					item.continent(),
					item.capitalCity(),
					item.referenceLatitude(),
					item.referenceLongitude(),
					item.referenceType(),
					item.population(),
					document.metadata().populationYear()
				));
				insertedCount++;
				continue;
			}

			existingCountry.synchronize(
				item.iso2Code(),
				item.iso3Code(),
				item.nameKr(),
				item.nameEn(),
				item.continent(),
				item.capitalCity(),
				item.referenceLatitude(),
				item.referenceLongitude(),
				item.referenceType(),
				item.population(),
				document.metadata().populationYear()
			);
			countriesToPersist.add(existingCountry);
			updatedCount++;
		}

		List<Country> countriesToDelete = existingCountriesByIso3.values().stream()
			.filter(country -> !seedIso3Codes.contains(country.getIso3Code()))
			.toList();

		if (!countriesToDelete.isEmpty()) {
			countryRepository.deleteAllInBatch(countriesToDelete);
		}

		countryRepository.saveAll(countriesToPersist);
		log.info(
			"Synchronized {} countries from {}. inserted={}, updated={}, deleted={}.",
			countriesToPersist.size(),
			countrySeedProperties.getLocation(),
			insertedCount,
			updatedCount,
			countriesToDelete.size()
		);
	}
}
