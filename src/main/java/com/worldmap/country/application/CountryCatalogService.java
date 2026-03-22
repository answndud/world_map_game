package com.worldmap.country.application;

import com.worldmap.common.exception.ResourceNotFoundException;
import com.worldmap.country.domain.Country;
import com.worldmap.country.domain.CountryRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CountryCatalogService {

	private final CountryRepository countryRepository;

	public CountryCatalogService(CountryRepository countryRepository) {
		this.countryRepository = countryRepository;
	}

	public List<CountrySummaryView> getCountries() {
		return countryRepository.findAllByOrderByNameKrAsc().stream()
			.map(country -> new CountrySummaryView(
				country.getIso3Code(),
				country.getNameKr(),
				country.getContinent(),
				country.getPopulation(),
				country.getPopulationYear()
			))
			.toList();
	}

	public CountryDetailView getCountry(String iso3Code) {
		Country country = countryRepository.findByIso3CodeIgnoreCase(iso3Code)
			.orElseThrow(() -> new ResourceNotFoundException("국가를 찾을 수 없습니다: " + iso3Code));

		return new CountryDetailView(
			country.getIso2Code(),
			country.getIso3Code(),
			country.getNameKr(),
			country.getNameEn(),
			country.getContinent(),
			country.getCapitalCity(),
			country.getReferenceLatitude(),
			country.getReferenceLongitude(),
			country.getReferenceType(),
			country.getPopulation(),
			country.getPopulationYear()
		);
	}
}
