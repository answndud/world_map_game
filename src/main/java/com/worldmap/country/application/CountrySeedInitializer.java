package com.worldmap.country.application;

import com.worldmap.country.domain.Country;
import com.worldmap.country.domain.CountryRepository;
import com.worldmap.country.infrastructure.CountrySeedReader;
import com.worldmap.country.infrastructure.CountrySeedReader.CountrySeedDocument;
import jakarta.transaction.Transactional;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
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

		if (countryRepository.count() > 0) {
			log.info("Country table already has data. Seed loading skipped.");
			return;
		}

		CountrySeedDocument document = countrySeedReader.read(countrySeedProperties.getLocation());
		countrySeedValidator.validate(document);

		List<Country> countries = document.countries().stream()
			.map(item -> Country.create(
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
			))
			.toList();

		countryRepository.saveAll(countries);
		log.info("Loaded {} countries from {}.", countries.size(), countrySeedProperties.getLocation());
	}
}
