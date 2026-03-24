package com.worldmap.recommendation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.worldmap.recommendation.domain.RecommendationFeedback;
import com.worldmap.recommendation.domain.RecommendationFeedbackRepository;
import com.worldmap.recommendation.domain.RecommendationSurveyAnswers;
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

	@Test
	void feedbackSummaryApiAggregatesBySurveyAndEngineVersion() throws Exception {
		saveFeedback("survey-v1", "engine-v1", 5);
		saveFeedback("survey-v1", "engine-v1", 4);
		saveFeedback("survey-v1", "engine-v1", 2);
		saveFeedback("survey-v2", "engine-v2", 3);
		saveFeedback("survey-v2", "engine-v2", 3);

		mockMvc.perform(get("/api/recommendation/feedback/summary"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.totalResponses").value(5))
			.andExpect(jsonPath("$.trackedVersionCount").value(2))
			.andExpect(jsonPath("$.overallAverageSatisfaction", closeTo(3.4, 0.001)))
			.andExpect(jsonPath("$.versionSummaries[?(@.surveyVersion=='survey-v1' && @.engineVersion=='engine-v1')].responseCount").value(hasItem(3)))
			.andExpect(jsonPath("$.versionSummaries[?(@.surveyVersion=='survey-v1' && @.engineVersion=='engine-v1')].averageSatisfaction").value(hasItem(closeTo(3.6666666667, 0.001))))
			.andExpect(jsonPath("$.versionSummaries[?(@.surveyVersion=='survey-v1' && @.engineVersion=='engine-v1')].score5Count").value(hasItem(1)))
			.andExpect(jsonPath("$.versionSummaries[?(@.surveyVersion=='survey-v1' && @.engineVersion=='engine-v1')].score4Count").value(hasItem(1)))
			.andExpect(jsonPath("$.versionSummaries[?(@.surveyVersion=='survey-v1' && @.engineVersion=='engine-v1')].score2Count").value(hasItem(1)))
			.andExpect(jsonPath("$.versionSummaries[?(@.surveyVersion=='survey-v2' && @.engineVersion=='engine-v2')].responseCount").value(hasItem(2)));
	}

	@Test
	void feedbackInsightsPageRendersVersionSummaryTable() throws Exception {
		saveFeedback("survey-v1", "engine-v1", 5);
		saveFeedback("survey-v1", "engine-v1", 4);

		mockMvc.perform(get("/recommendation/feedback-insights"))
			.andExpect(status().isOk())
			.andExpect(view().name("recommendation/feedback-insights"))
			.andExpect(content().string(containsString("추천 만족도 집계")))
			.andExpect(content().string(containsString("survey-v1")))
			.andExpect(content().string(containsString("engine-v1")))
			.andExpect(content().string(containsString("TOTAL FEEDBACK")));
	}

	private void saveFeedback(String surveyVersion, String engineVersion, int score) {
		recommendationFeedbackRepository.save(
			RecommendationFeedback.create(
				surveyVersion,
				engineVersion,
				score,
				new RecommendationSurveyAnswers(
					RecommendationSurveyAnswers.ClimatePreference.WARM,
					RecommendationSurveyAnswers.PacePreference.BALANCED,
					RecommendationSurveyAnswers.BudgetPreference.LOW,
					RecommendationSurveyAnswers.EnvironmentPreference.CITY,
					RecommendationSurveyAnswers.EnglishImportance.MEDIUM,
					RecommendationSurveyAnswers.PriorityFocus.FOOD
				)
			)
		);
	}
}
