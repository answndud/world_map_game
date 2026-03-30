package com.worldmap.recommendation;

import static com.worldmap.auth.application.MemberSessionManager.MEMBER_ID_ATTRIBUTE;
import static com.worldmap.auth.application.MemberSessionManager.MEMBER_NICKNAME_ATTRIBUTE;
import static com.worldmap.auth.application.MemberSessionManager.MEMBER_ROLE_ATTRIBUTE;
import static com.worldmap.auth.domain.MemberRole.ADMIN;
import static com.worldmap.auth.domain.MemberRole.USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.worldmap.recommendation.application.RecommendationSurveyService;
import com.worldmap.recommendation.domain.RecommendationFeedback;
import com.worldmap.recommendation.domain.RecommendationFeedbackRepository;
import com.worldmap.recommendation.domain.RecommendationSurveyAnswers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

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
	void feedbackApiStoresAnonymousSatisfactionAndAnswerSnapshotFromServerSideContext() throws Exception {
		MockHttpSession browserSession = new MockHttpSession();
		String feedbackToken = issueFeedbackToken(browserSession);

		mockMvc.perform(post("/api/recommendation/feedback")
				.session(browserSession)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "feedbackToken": "%s",
					  "satisfactionScore": 4
					}
					""".formatted(feedbackToken)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.satisfactionScore").value(4))
			.andExpect(jsonPath("$.surveyVersion").value("survey-v4"))
			.andExpect(jsonPath("$.engineVersion").value(RecommendationSurveyService.ENGINE_VERSION));

		assertThat(recommendationFeedbackRepository.findAll()).hasSize(1);
		RecommendationFeedback feedback = recommendationFeedbackRepository.findAll().getFirst();
		assertThat(feedback.getSatisfactionScore()).isEqualTo(4);
		assertThat(feedback.getSurveyVersion()).isEqualTo("survey-v4");
		assertThat(feedback.getEngineVersion()).isEqualTo(RecommendationSurveyService.ENGINE_VERSION);
		assertThat(feedback.getClimatePreference().name()).isEqualTo("WARM");
		assertThat(feedback.getSeasonStylePreference().name()).isEqualTo("STABLE");
		assertThat(feedback.getSeasonTolerance().name()).isEqualTo("HIGH");
		assertThat(feedback.getFoodImportance().name()).isEqualTo("MEDIUM");
		assertThat(feedback.getCultureLeisureImportance().name()).isEqualTo("HIGH");
		assertThat(feedback.getSettlementPreference().name()).isEqualTo("BALANCED");
		assertThat(feedback.getFutureBasePreference().name()).isEqualTo("BALANCED");
	}

	@Test
	void feedbackApiRejectsOutOfRangeScore() throws Exception {
		MockHttpSession browserSession = new MockHttpSession();
		String feedbackToken = issueFeedbackToken(browserSession);

		mockMvc.perform(post("/api/recommendation/feedback")
				.session(browserSession)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "feedbackToken": "%s",
					  "satisfactionScore": 6
					}
					""".formatted(feedbackToken)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("만족도 점수는 5점 이하여야 합니다.")));

		assertThat(recommendationFeedbackRepository.count()).isZero();
	}

	@Test
	void feedbackApiRejectsUnknownFeedbackToken() throws Exception {
		mockMvc.perform(post("/api/recommendation/feedback")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "feedbackToken": "unknown-token",
					  "satisfactionScore": 4
					}
					"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("추천 결과 컨텍스트가 만료되었거나 올바르지 않습니다.")));

		assertThat(recommendationFeedbackRepository.count()).isZero();
	}

	@Test
	void feedbackSummaryApiAggregatesBySurveyAndEngineVersionForAdminOnly() throws Exception {
		saveFeedback("survey-v1", "engine-v1", 5);
		saveFeedback("survey-v1", "engine-v1", 4);
		saveFeedback("survey-v1", "engine-v1", 2);
		saveFeedback("survey-v4", RecommendationSurveyService.ENGINE_VERSION, 3);
		saveFeedback("survey-v4", RecommendationSurveyService.ENGINE_VERSION, 3);

		mockMvc.perform(get("/api/recommendation/feedback/summary").session(adminSession()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.totalResponses").value(5))
			.andExpect(jsonPath("$.trackedVersionCount").value(2))
			.andExpect(jsonPath("$.overallAverageSatisfaction", closeTo(3.4, 0.001)))
			.andExpect(jsonPath("$.versionSummaries[?(@.surveyVersion=='survey-v1' && @.engineVersion=='engine-v1')].responseCount").value(hasItem(3)))
			.andExpect(jsonPath("$.versionSummaries[?(@.surveyVersion=='survey-v1' && @.engineVersion=='engine-v1')].averageSatisfaction").value(hasItem(closeTo(3.6666666667, 0.001))))
			.andExpect(jsonPath("$.versionSummaries[?(@.surveyVersion=='survey-v1' && @.engineVersion=='engine-v1')].score5Count").value(hasItem(1)))
			.andExpect(jsonPath("$.versionSummaries[?(@.surveyVersion=='survey-v1' && @.engineVersion=='engine-v1')].score4Count").value(hasItem(1)))
			.andExpect(jsonPath("$.versionSummaries[?(@.surveyVersion=='survey-v1' && @.engineVersion=='engine-v1')].score2Count").value(hasItem(1)))
			.andExpect(jsonPath("$.versionSummaries[?(@.surveyVersion=='survey-v4' && @.engineVersion=='%s')].responseCount".formatted(RecommendationSurveyService.ENGINE_VERSION)).value(hasItem(2)));
	}

	@Test
	void feedbackSummaryApiRejectsNonAdminSession() throws Exception {
		mockMvc.perform(get("/api/recommendation/feedback/summary").session(userSession()))
			.andExpect(status().isForbidden());
	}

	@Test
	void legacyFeedbackInsightsRouteRedirectsToDashboardRecommendationFeedbackPage() throws Exception {
		mockMvc.perform(get("/recommendation/feedback-insights"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/dashboard/recommendation/feedback"));
	}

	private void saveFeedback(String surveyVersion, String engineVersion, int score) {
		recommendationFeedbackRepository.save(
			RecommendationFeedback.create(
				surveyVersion,
				engineVersion,
				score,
				new RecommendationSurveyAnswers(
					RecommendationSurveyAnswers.ClimatePreference.WARM,
					RecommendationSurveyAnswers.SeasonStylePreference.STABLE,
					RecommendationSurveyAnswers.SeasonTolerance.MEDIUM,
					RecommendationSurveyAnswers.PacePreference.BALANCED,
					RecommendationSurveyAnswers.CrowdPreference.BALANCED,
					RecommendationSurveyAnswers.CostQualityPreference.VALUE_FIRST,
					RecommendationSurveyAnswers.HousingPreference.SPACE_FIRST,
					RecommendationSurveyAnswers.EnvironmentPreference.CITY,
					RecommendationSurveyAnswers.MobilityPreference.BALANCED,
					RecommendationSurveyAnswers.EnglishSupportNeed.MEDIUM,
					RecommendationSurveyAnswers.NewcomerSupportNeed.MEDIUM,
					RecommendationSurveyAnswers.ImportanceLevel.LOW,
					RecommendationSurveyAnswers.ImportanceLevel.LOW,
					RecommendationSurveyAnswers.ImportanceLevel.MEDIUM,
					RecommendationSurveyAnswers.ImportanceLevel.HIGH,
					RecommendationSurveyAnswers.ImportanceLevel.MEDIUM,
					RecommendationSurveyAnswers.ImportanceLevel.MEDIUM,
					RecommendationSurveyAnswers.WorkLifePreference.BALANCED,
					RecommendationSurveyAnswers.SettlementPreference.BALANCED,
					RecommendationSurveyAnswers.FutureBasePreference.BALANCED
				)
			)
		);
	}

	private String issueFeedbackToken(MockHttpSession browserSession) throws Exception {
		MvcResult result = mockMvc.perform(post("/recommendation/survey")
				.session(browserSession)
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
			.andReturn();

		String html = result.getResponse().getContentAsString();
		java.util.regex.Matcher matcher = java.util.regex.Pattern
			.compile("name=\"feedbackToken\" value=\"([^\"]+)\"")
			.matcher(html);
		assertThat(matcher.find()).isTrue();
		return matcher.group(1);
	}

	private MockHttpSession adminSession() {
		MockHttpSession session = new MockHttpSession();
		session.setAttribute(MEMBER_ID_ATTRIBUTE, 1L);
		session.setAttribute(MEMBER_NICKNAME_ATTRIBUTE, "worldmap_admin");
		session.setAttribute(MEMBER_ROLE_ATTRIBUTE, ADMIN.name());
		return session;
	}

	private MockHttpSession userSession() {
		MockHttpSession session = new MockHttpSession();
		session.setAttribute(MEMBER_ID_ATTRIBUTE, 2L);
		session.setAttribute(MEMBER_NICKNAME_ATTRIBUTE, "orbit_runner");
		session.setAttribute(MEMBER_ROLE_ATTRIBUTE, USER.name());
		return session;
	}
}
