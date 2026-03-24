package com.worldmap.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.worldmap.auth.domain.Member;
import com.worldmap.auth.domain.MemberRepository;
import com.worldmap.auth.domain.MemberRole;
import com.worldmap.auth.application.MemberPasswordHasher;
import com.worldmap.country.domain.CountryRepository;
import com.worldmap.game.location.domain.LocationGameSession;
import com.worldmap.game.location.domain.LocationGameSessionRepository;
import com.worldmap.game.location.domain.LocationGameStage;
import com.worldmap.game.location.domain.LocationGameStageRepository;
import com.worldmap.ranking.domain.LeaderboardRecord;
import com.worldmap.ranking.domain.LeaderboardRecordRepository;
import java.util.UUID;
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
class AuthFlowIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private MemberPasswordHasher memberPasswordHasher;

	@Autowired
	private LocationGameSessionRepository locationGameSessionRepository;

	@Autowired
	private LocationGameStageRepository locationGameStageRepository;

	@Autowired
	private LeaderboardRecordRepository leaderboardRecordRepository;

	@Autowired
	private CountryRepository countryRepository;

	@BeforeEach
	void clearMembers() {
		memberRepository.deleteAll();
		leaderboardRecordRepository.deleteAll();
	}

	@Test
	void signupCreatesSimpleAccountAndKeepsMemberSession() throws Exception {
		MockHttpSession browserSession = new MockHttpSession();

		mockMvc.perform(
			post("/signup")
				.session(browserSession)
				.param("nickname", "orbit_runner")
				.param("password", "secret1234")
		)
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/mypage"));

		Member member = memberRepository.findByNicknameIgnoreCase("orbit_runner").orElseThrow();
		assertThat(member.getPasswordHash()).isNotEqualTo("secret1234");
		assertThat(member.getLastLoginAt()).isNotNull();

		mockMvc.perform(get("/mypage").session(browserSession))
			.andExpect(status().isOk())
			.andExpect(view().name("mypage"))
			.andExpect(content().string(containsString("orbit_runner")))
			.andExpect(content().string(containsString("로그아웃")));
	}

	@Test
	void loginFailureStaysOnLoginPageWithErrorMessage() throws Exception {
		memberRepository.save(Member.create("orbit_runner", "$2a$10$e0NRzHX0tM5f0Y4b9Kz6uOrUrs9jwELyhl725LLJoPLD114F8CbnW", com.worldmap.auth.domain.MemberRole.USER));

		mockMvc.perform(
			post("/login")
				.param("nickname", "orbit_runner")
				.param("password", "wrong-pass")
		)
			.andExpect(status().isOk())
			.andExpect(view().name("auth/login"))
			.andExpect(content().string(containsString("닉네임 또는 비밀번호가 올바르지 않습니다.")));
	}

	@Test
	void adminLoginRedirectsBackToRequestedAdminRoute() throws Exception {
		memberRepository.save(
			Member.create(
				"worldmap_admin",
				memberPasswordHasher.hash("secret123"),
				MemberRole.ADMIN
			)
		);

		mockMvc.perform(
			post("/login")
				.param("nickname", "worldmap_admin")
				.param("password", "secret123")
				.param("returnTo", "/admin")
		)
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/admin"));
	}

	@Test
	void signupClaimsCurrentGuestRecordsIntoMemberOwnership() throws Exception {
		MockHttpSession browserSession = new MockHttpSession();
		UUID guestSessionId = UUID.fromString(startLocationGame("guest_runner", browserSession));
		LocationGameSession guestSession = locationGameSessionRepository.findById(guestSessionId).orElseThrow();
		String guestSessionKey = guestSession.getGuestSessionKey();
		assertThat(guestSessionKey).isNotBlank();

		LocationGameStage firstStage = locationGameStageRepository.findBySessionIdAndStageNumber(guestSessionId, 1)
			.orElseThrow();
		String wrongCountryIso3Code = countryRepository.findAll().stream()
			.map(country -> country.getIso3Code())
			.filter(iso3Code -> !iso3Code.equals(firstStage.getTargetCountryIso3Code()))
			.findFirst()
			.orElseThrow();

		for (int attempt = 1; attempt <= 3; attempt++) {
			mockMvc.perform(
				post("/api/games/location/sessions/{sessionId}/answer", guestSessionId)
					.session(browserSession)
					.contentType("application/json")
					.content("""
						{
						  "stageNumber": 1,
						  "selectedCountryIso3Code": "%s"
						}
						""".formatted(wrongCountryIso3Code))
			)
				.andExpect(status().isOk());
		}

		mockMvc.perform(
			post("/signup")
				.session(browserSession)
				.param("nickname", "claimed_runner")
				.param("password", "secret1234")
		)
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/mypage"));

		Member member = memberRepository.findByNicknameIgnoreCase("claimed_runner").orElseThrow();
		LocationGameSession claimedSession = locationGameSessionRepository.findById(guestSessionId).orElseThrow();
		LeaderboardRecord claimedRecord = leaderboardRecordRepository.findAll().getFirst();

		assertThat(claimedSession.getMemberId()).isEqualTo(member.getId());
		assertThat(claimedSession.getGuestSessionKey()).isNull();
		assertThat(claimedSession.getPlayerNickname()).isEqualTo("guest_runner");

		assertThat(claimedRecord.getMemberId()).isEqualTo(member.getId());
		assertThat(claimedRecord.getGuestSessionKey()).isNull();
		assertThat(claimedRecord.getPlayerNickname()).isEqualTo("guest_runner");

		mockMvc.perform(get("/mypage").session(browserSession))
			.andExpect(status().isOk())
			.andExpect(view().name("mypage"))
			.andExpect(content().string(containsString("총 완료 플레이")))
			.andExpect(content().string(containsString("1회")))
			.andExpect(content().string(containsString("국가 위치 찾기")))
			.andExpect(content().string(containsString("최근 플레이")))
			.andExpect(content().string(containsString("#1")));
	}

	private String startLocationGame(String nickname, MockHttpSession browserSession) throws Exception {
		return com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
			.readTree(
				mockMvc.perform(
					post("/api/games/location/sessions")
						.session(browserSession)
						.contentType("application/json")
						.content("{\"nickname\":\"" + nickname + "\"}")
				)
					.andExpect(status().isCreated())
					.andReturn()
					.getResponse()
					.getContentAsString()
			)
			.get("sessionId")
			.asText();
	}
}
