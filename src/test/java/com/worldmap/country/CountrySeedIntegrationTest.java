package com.worldmap.country;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.worldmap.country.application.CountrySeedInitializer;
import com.worldmap.country.domain.Continent;
import com.worldmap.country.domain.Country;
import com.worldmap.country.domain.CountryReferenceType;
import com.worldmap.country.domain.CountryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CountrySeedIntegrationTest {

	@Autowired
	private CountryRepository countryRepository;

	@Autowired
	private CountrySeedInitializer countrySeedInitializer;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@BeforeEach
	void resetSeedState() {
		countrySeedInitializer.run(new DefaultApplicationArguments(new String[0]));
	}

	@Test
	void countrySeedLoadsOnStartup() {
		assertThat(countryRepository.count()).isEqualTo(194);

		Country korea = countryRepository.findByIso3CodeIgnoreCase("kor").orElseThrow();
		Country france = countryRepository.findByIso3CodeIgnoreCase("fra").orElseThrow();
		Country norway = countryRepository.findByIso3CodeIgnoreCase("nor").orElseThrow();

		assertThat(korea.getNameKr()).isEqualTo("대한민국");
		assertThat(korea.getPopulationYear()).isEqualTo(2024);
		assertThat(france.getNameKr()).isEqualTo("프랑스");
		assertThat(norway.getNameKr()).isEqualTo("노르웨이");
	}

	@Test
	void seedInitializerSynchronizesExistingRows() {
		countryRepository.save(Country.create(
			"ZZ",
			"ZZZ",
			"테스트국",
			"Testland",
			Continent.EUROPE,
			"Test City",
			new java.math.BigDecimal("10.0000"),
			new java.math.BigDecimal("20.0000"),
			CountryReferenceType.CAPITAL_CITY,
			1234L,
			2024
		));
		jdbcTemplate.update("update country set name_kr = ? where iso3_code = ?", "테스트대한민국", "KOR");

		countrySeedInitializer.run(new DefaultApplicationArguments(new String[0]));

		assertThat(countryRepository.count()).isEqualTo(194);
		assertThat(countryRepository.findByIso3CodeIgnoreCase("ZZZ")).isEmpty();
		assertThat(countryRepository.findByIso3CodeIgnoreCase("KOR"))
			.get()
			.extracting(Country::getNameKr)
			.isEqualTo("대한민국");
	}

	@Test
	void countryApiReturnsCountryDetails() throws Exception {
		mockMvc.perform(get("/api/countries/KOR"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.iso3Code").value("KOR"))
			.andExpect(jsonPath("$.nameKr").value("대한민국"))
			.andExpect(jsonPath("$.referenceType").value("CAPITAL_CITY"))
			.andExpect(jsonPath("$.populationYear").value(2024));
	}

	@Test
	void countryApiReturnsNotFoundForUnknownIso3Code() throws Exception {
		mockMvc.perform(get("/api/countries/XYZ"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.message").value("국가를 찾을 수 없습니다: XYZ"));
	}
}
