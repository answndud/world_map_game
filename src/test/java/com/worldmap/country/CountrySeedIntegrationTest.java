package com.worldmap.country;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.worldmap.country.application.CountrySeedInitializer;
import com.worldmap.country.domain.Country;
import com.worldmap.country.domain.CountryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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

	@Test
	void countrySeedLoadsOnStartup() {
		assertThat(countryRepository.count()).isEqualTo(17);

		Country korea = countryRepository.findByIso3CodeIgnoreCase("kor").orElseThrow();

		assertThat(korea.getNameKr()).isEqualTo("대한민국");
		assertThat(korea.getPopulationYear()).isEqualTo(2024);
	}

	@Test
	void seedInitializerIsIdempotent() throws Exception {
		long before = countryRepository.count();

		countrySeedInitializer.run(new DefaultApplicationArguments(new String[0]));

		assertThat(countryRepository.count()).isEqualTo(before);
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
