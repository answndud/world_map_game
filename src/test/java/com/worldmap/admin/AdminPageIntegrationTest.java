package com.worldmap.admin;

import static com.worldmap.auth.application.MemberSessionManager.MEMBER_ID_ATTRIBUTE;
import static com.worldmap.auth.application.MemberSessionManager.MEMBER_NICKNAME_ATTRIBUTE;
import static com.worldmap.auth.application.MemberSessionManager.MEMBER_ROLE_ATTRIBUTE;
import static com.worldmap.auth.domain.MemberRole.ADMIN;
import static com.worldmap.auth.domain.MemberRole.USER;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
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
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminPageIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private RecommendationFeedbackRepository recommendationFeedbackRepository;

	@BeforeEach
	void setUp() {
		recommendationFeedbackRepository.deleteAll();
	}

	@Test
	void adminDashboardRendersCurrentRecommendationOpsOverview() throws Exception {
		mockMvc.perform(get("/admin").session(adminSession()))
			.andExpect(status().isOk())
			.andExpect(view().name("admin/index"))
			.andExpect(model().attributeExists("dashboard"))
			.andExpect(content().string(containsString("관리자 대시보드")))
			.andExpect(content().string(containsString("추천 운영 상태")))
			.andExpect(content().string(containsString("survey-v2")))
			.andExpect(content().string(containsString("engine-v2")))
			.andExpect(content().string(containsString("ADMIN` role 세션으로만 접근 가능")));
	}

	@Test
	void adminRecommendationFeedbackPageRendersVersionSummaryTable() throws Exception {
		saveFeedback("survey-v1", "engine-v1", 5);
		saveFeedback("survey-v1", "engine-v1", 4);

		mockMvc.perform(get("/admin/recommendation/feedback").session(adminSession()))
			.andExpect(status().isOk())
			.andExpect(view().name("admin/recommendation-feedback"))
			.andExpect(model().attributeExists("dashboard"))
			.andExpect(model().attributeExists("feedbackInsights"))
			.andExpect(content().string(containsString("추천 만족도 운영 화면")))
			.andExpect(content().string(containsString("버전 조합별 집계")))
			.andExpect(content().string(containsString("survey-v1")))
			.andExpect(content().string(containsString("engine-v1")));
	}

	@Test
	void adminPersonaBaselinePageRendersWeakAndActiveSignalSections() throws Exception {
		mockMvc.perform(get("/admin/recommendation/persona-baseline").session(adminSession()))
			.andExpect(status().isOk())
			.andExpect(view().name("admin/recommendation-persona-baseline"))
			.andExpect(model().attributeExists("dashboard"))
			.andExpect(model().attributeExists("personaBaseline"))
			.andExpect(content().string(containsString("추천 baseline 운영 화면")))
			.andExpect(content().string(containsString("P04")))
			.andExpect(content().string(containsString("P15")))
			.andExpect(content().string(containsString("15 / 18")));
	}

	@Test
	void adminRoutesRedirectUnauthenticatedUsersToLogin() throws Exception {
		mockMvc.perform(get("/admin"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/login?returnTo=/admin"));
	}

	@Test
	void adminRoutesRejectNonAdminMembers() throws Exception {
		mockMvc.perform(get("/admin").session(userSession()))
			.andExpect(status().isForbidden());
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
					RecommendationSurveyAnswers.PriorityFocus.FOOD,
					RecommendationSurveyAnswers.SettlementPreference.BALANCED,
					RecommendationSurveyAnswers.MobilityPreference.BALANCED
				)
			)
		);
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
