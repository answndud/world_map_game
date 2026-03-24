package com.worldmap.recommendation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.worldmap.recommendation.domain.RecommendationFeedback;
import com.worldmap.recommendation.domain.RecommendationFeedbackRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RecommendationFeedbackIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private RecommendationFeedbackRepository recommendationFeedbackRepository;

	@BeforeEach
	void setUp() {
		recommendationFeedbackRepository.deleteAll();
	}

	@Test
	void feedbackApiStoresAnonymousSatisfactionAndAnswerSnapshot() throws Exception {
		mockMvc.perform(post("/api/recommendation/feedback")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "surveyVersion": "survey-v1",
					  "engineVersion": "engine-v1",
					  "satisfactionScore": 4,
					  "climatePreference": "WARM",
					  "pacePreference": "BALANCED",
					  "budgetPreference": "LOW",
					  "environmentPreference": "CITY",
					  "englishImportance": "MEDIUM",
					  "priorityFocus": "FOOD"
					}
					"""))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.satisfactionScore").value(4))
			.andExpect(jsonPath("$.surveyVersion").value("survey-v1"))
			.andExpect(jsonPath("$.engineVersion").value("engine-v1"));

		assertThat(recommendationFeedbackRepository.findAll()).hasSize(1);
		RecommendationFeedback feedback = recommendationFeedbackRepository.findAll().getFirst();
		assertThat(feedback.getSatisfactionScore()).isEqualTo(4);
		assertThat(feedback.getSurveyVersion()).isEqualTo("survey-v1");
		assertThat(feedback.getEngineVersion()).isEqualTo("engine-v1");
		assertThat(feedback.getClimatePreference().name()).isEqualTo("WARM");
		assertThat(feedback.getPriorityFocus().name()).isEqualTo("FOOD");
	}

	@Test
	void feedbackApiRejectsOutOfRangeScore() throws Exception {
		mockMvc.perform(post("/api/recommendation/feedback")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "surveyVersion": "survey-v1",
					  "engineVersion": "engine-v1",
					  "satisfactionScore": 6,
					  "climatePreference": "WARM",
					  "pacePreference": "BALANCED",
					  "budgetPreference": "LOW",
					  "environmentPreference": "CITY",
					  "englishImportance": "MEDIUM",
					  "priorityFocus": "FOOD"
					}
					"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("만족도 점수는 5점 이하여야 합니다.")));

		assertThat(recommendationFeedbackRepository.count()).isZero();
	}
}
