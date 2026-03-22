package com.worldmap.country.web;

import com.worldmap.country.application.CountryCatalogService;
import com.worldmap.country.application.CountryDetailView;
import com.worldmap.country.application.CountrySummaryView;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/countries")
public class CountryApiController {

	private final CountryCatalogService countryCatalogService;

	public CountryApiController(CountryCatalogService countryCatalogService) {
		this.countryCatalogService = countryCatalogService;
	}

	@GetMapping
	public List<CountrySummaryView> countries() {
		return countryCatalogService.getCountries();
	}

	@GetMapping("/{iso3Code}")
	public CountryDetailView country(@PathVariable String iso3Code) {
		return countryCatalogService.getCountry(iso3Code);
	}
}
