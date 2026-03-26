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

import com.worldmap.auth.domain.Member;
import com.worldmap.auth.domain.MemberRepository;
import com.worldmap.game.location.domain.LocationGameSession;
import com.worldmap.game.location.domain.LocationGameSessionRepository;
import com.worldmap.game.population.domain.PopulationGameSession;
import com.worldmap.game.population.domain.PopulationGameSessionRepository;
import com.worldmap.ranking.domain.LeaderboardGameLevel;
import com.worldmap.ranking.domain.LeaderboardGameMode;
import com.worldmap.ranking.domain.LeaderboardRecord;
import com.worldmap.ranking.domain.LeaderboardRecordRepository;
import com.worldmap.recommendation.domain.RecommendationFeedback;
import com.worldmap.recommendation.domain.RecommendationFeedbackRepository;
import com.worldmap.recommendation.domain.RecommendationSurveyAnswers;
import java.time.LocalDateTime;
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

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private LocationGameSessionRepository locationGameSessionRepository;

	@Autowired
	private PopulationGameSessionRepository populationGameSessionRepository;

	@Autowired
	private LeaderboardRecordRepository leaderboardRecordRepository;

	@BeforeEach
	void setUp() {
		recommendationFeedbackRepository.deleteAll();
		leaderboardRecordRepository.deleteAll();
		locationGameSessionRepository.deleteAll();
		populationGameSessionRepository.deleteAll();
		memberRepository.deleteAll();
	}

	@Test
	void adminDashboardRendersCurrentRecommendationOpsOverview() throws Exception {
		memberRepository.save(Member.create("member_one", "hash", USER));
		memberRepository.save(Member.create("member_two", "hash", USER));

		LocationGameSession activeMemberLocation = LocationGameSession.ready("member_one", 1L, null, 5);
		activeMemberLocation.startGame(LocalDateTime.now().minusHours(2));
		locationGameSessionRepository.save(activeMemberLocation);

		PopulationGameSession activeGuestPopulation = PopulationGameSession.ready("guest_one", null, "guest-key-1", 5);
		activeGuestPopulation.startGame(LocalDateTime.now().minusHours(1));
		populationGameSessionRepository.save(activeGuestPopulation);

		LeaderboardRecord todayLocationRun = LeaderboardRecord.create(
			"run-location-today",
			activeMemberLocation.getId(),
			LeaderboardGameMode.LOCATION,
			LeaderboardGameLevel.LEVEL_1,
			"member_one",
			1L,
			null,
			320,
			320004L,
			4,
			6,
			LocalDateTime.now().minusMinutes(30)
		);
		LeaderboardRecord todayPopulationRun = LeaderboardRecord.create(
			"run-population-today",
			activeGuestPopulation.getId(),
			LeaderboardGameMode.POPULATION,
			LeaderboardGameLevel.LEVEL_1,
			"guest_one",
			null,
			"guest-key-1",
			280,
			280004L,
			3,
			5,
			LocalDateTime.now().minusMinutes(10)
		);
		leaderboardRecordRepository.save(todayLocationRun);
		leaderboardRecordRepository.save(todayPopulationRun);

		mockMvc.perform(get("/dashboard").session(adminSession()))
			.andExpect(status().isOk())
			.andExpect(view().name("admin/index"))
			.andExpect(model().attributeExists("dashboard"))
			.andExpect(content().string(containsString("운영 Dashboard")))
			.andExpect(content().string(containsString("서비스 활동 요약")))
			.andExpect(content().string(containsString("TOTAL MEMBERS")))
			.andExpect(content().string(containsString(">2<")))
			.andExpect(content().string(containsString("TODAY ACTIVE MEMBERS")))
			.andExpect(content().string(containsString("TODAY ACTIVE GUESTS")))
			.andExpect(content().string(containsString("TODAY COMPLETED RUNS")))
			.andExpect(content().string(containsString("L 1 / P 1")))
			.andExpect(content().string(containsString("추천 운영 상태")))
			.andExpect(content().string(containsString("survey-v4")))
			.andExpect(content().string(containsString("engine-v17")))
			.andExpect(content().string(containsString("Dashboard 화면은 `ADMIN` role 세션으로만 접근 가능하게 보호한다.")));
	}

	@Test
	void adminRecommendationFeedbackPageRendersVersionSummaryTable() throws Exception {
		saveFeedback("survey-v1", "engine-v1", 5);
		saveFeedback("survey-v1", "engine-v1", 4);

		mockMvc.perform(get("/dashboard/recommendation/feedback").session(adminSession()))
			.andExpect(status().isOk())
			.andExpect(view().name("admin/recommendation-feedback"))
			.andExpect(model().attributeExists("dashboard"))
			.andExpect(model().attributeExists("feedbackInsights"))
			.andExpect(model().attributeExists("opsReview"))
			.andExpect(content().string(containsString("추천 만족도 운영 화면")))
			.andExpect(content().string(containsString("운영 판단 메모")))
			.andExpect(content().string(containsString("현재 버전 피드백 더 수집")))
			.andExpect(content().string(containsString("버전 조합별 집계")))
			.andExpect(content().string(containsString("survey-v1")))
			.andExpect(content().string(containsString("engine-v1")));
	}

	@Test
	void adminPersonaBaselinePageRendersWeakAndActiveSignalSections() throws Exception {
		mockMvc.perform(get("/dashboard/recommendation/persona-baseline").session(adminSession()))
			.andExpect(status().isOk())
			.andExpect(view().name("admin/recommendation-persona-baseline"))
			.andExpect(model().attributeExists("dashboard"))
			.andExpect(model().attributeExists("personaBaseline"))
			.andExpect(content().string(containsString("추천 baseline 운영 화면")))
			.andExpect(content().string(containsString("자동 계산")))
			.andExpect(content().string(containsString("ANCHOR DRIFT")))
			.andExpect(content().string(containsString("1위 재검토 대상")))
			.andExpect(content().string(containsString("P11")))
			.andExpect(content().string(containsString("P15")))
			.andExpect(content().string(containsString("/ 18")));
	}

	@Test
	void adminRoutesRedirectUnauthenticatedUsersToLogin() throws Exception {
		mockMvc.perform(get("/dashboard"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/login?returnTo=/dashboard"));
	}

	@Test
	void adminRoutesRejectNonAdminMembers() throws Exception {
		mockMvc.perform(get("/dashboard").session(userSession()))
			.andExpect(status().isForbidden());
	}

	@Test
	void legacyAdminRouteRedirectsAdminSessionToDashboard() throws Exception {
		mockMvc.perform(get("/admin").session(adminSession()))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/dashboard"));
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
