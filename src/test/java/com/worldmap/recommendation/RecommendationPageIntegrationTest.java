package com.worldmap.recommendation;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RecommendationPageIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void surveyPageRenders() throws Exception {
		mockMvc.perform(get("/recommendation/survey"))
			.andExpect(status().isOk())
			.andExpect(view().name("recommendation/survey"))
			.andExpect(model().attributeExists("surveyForm"))
			.andExpect(model().attributeExists("surveyQuestions"))
			.andExpect(content().string(containsString("어울리는 나라 추천")));
	}

	@Test
	void surveySubmissionReturnsDeterministicResult() throws Exception {
		mockMvc.perform(post("/recommendation/survey")
				.param("climatePreference", "WARM")
				.param("pacePreference", "FAST")
				.param("budgetPreference", "HIGH")
				.param("environmentPreference", "CITY")
				.param("englishImportance", "HIGH")
				.param("priorityFocus", "DIVERSITY")
				.param("settlementPreference", "BALANCED")
				.param("mobilityPreference", "BALANCED"))
			.andExpect(status().isOk())
			.andExpect(view().name("recommendation/result"))
			.andExpect(model().attributeExists("result"))
			.andExpect(content().string(containsString("Top 3 국가")))
			.andExpect(content().string(containsString("싱가포르")))
			.andExpect(content().string(containsString("추천 만족도")))
			.andExpect(content().string(containsString("survey-v2")))
			.andExpect(content().string(containsString("engine-v2")));
	}
}
