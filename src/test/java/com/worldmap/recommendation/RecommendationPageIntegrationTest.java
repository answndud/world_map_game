package com.worldmap.recommendation;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.worldmap.recommendation.application.RecommendationSurveyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
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
			.andExpect(content().string(containsString("나에게 어울리는 국가 찾기")))
			.andExpect(content().string(containsString("Find Your Match")))
			.andExpect(content().string(containsString("20 Questions")))
			.andExpect(content().string(not(containsString("deterministic"))))
			.andExpect(content().string(not(containsString("Offline Eval"))));
	}

	@Test
	void surveySubmissionReturnsDeterministicResult() throws Exception {
		MockHttpSession session = new MockHttpSession();

		mockMvc.perform(post("/recommendation/survey")
				.session(session)
				.param("climatePreference", "WARM")
				.param("seasonStylePreference", "STABLE")
				.param("seasonTolerance", "HIGH")
				.param("pacePreference", "FAST")
				.param("crowdPreference", "LIVELY")
				.param("costQualityPreference", "QUALITY_FIRST")
				.param("housingPreference", "CENTER_FIRST")
				.param("environmentPreference", "CITY")
				.param("mobilityPreference", "TRANSIT_FIRST")
				.param("englishSupportNeed", "HIGH")
				.param("newcomerSupportNeed", "MEDIUM")
				.param("safetyPriority", "MEDIUM")
				.param("publicServicePriority", "MEDIUM")
				.param("digitalConveniencePriority", "HIGH")
				.param("foodImportance", "MEDIUM")
				.param("diversityImportance", "HIGH")
				.param("cultureLeisureImportance", "HIGH")
				.param("workLifePreference", "DRIVE_FIRST")
				.param("settlementPreference", "BALANCED")
				.param("futureBasePreference", "BALANCED"))
			.andExpect(status().isOk())
			.andExpect(view().name("recommendation/result"))
			.andExpect(model().attributeExists("result"))
			.andExpect(content().string(containsString("잘 맞는 나라 3곳")))
			.andExpect(content().string(containsString("싱가포르")))
			.andExpect(content().string(containsString("추천 만족도")))
			.andExpect(content().string(containsString("feedbackToken")))
			.andExpect(content().string(containsString("type=\"radio\" name=\"satisfactionScore\" value=\"1\"")))
			.andExpect(content().string(containsString("role=\"status\"")))
			.andExpect(content().string(not(containsString("recommendation-satisfaction-score"))))
			.andExpect(content().string(not(containsString("surveyVersion"))))
			.andExpect(content().string(not(containsString(RecommendationSurveyService.ENGINE_VERSION))))
			.andExpect(content().string(not(containsString("deterministic"))))
			.andExpect(content().string(not(containsString("만족도 집계 보기"))));
	}
}
