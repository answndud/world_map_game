package com.worldmap.auth;

import static com.worldmap.auth.application.MemberSessionManager.MEMBER_ID_ATTRIBUTE;
import static com.worldmap.auth.application.MemberSessionManager.MEMBER_NICKNAME_ATTRIBUTE;
import static com.worldmap.auth.application.MemberSessionManager.MEMBER_ROLE_ATTRIBUTE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
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
import com.worldmap.game.capital.domain.CapitalGameSession;
import com.worldmap.game.capital.domain.CapitalGameSessionRepository;
import com.worldmap.game.flag.domain.FlagGameSession;
import com.worldmap.game.flag.domain.FlagGameSessionRepository;
import com.worldmap.game.location.domain.LocationGameSession;
import com.worldmap.game.location.domain.LocationGameSessionRepository;
import com.worldmap.game.location.domain.LocationGameStage;
import com.worldmap.game.location.domain.LocationGameStageRepository;
import com.worldmap.game.population.domain.PopulationGameSession;
import com.worldmap.game.population.domain.PopulationGameSessionRepository;
import com.worldmap.game.populationbattle.domain.PopulationBattleGameSession;
import com.worldmap.game.populationbattle.domain.PopulationBattleGameSessionRepository;
import com.worldmap.ranking.domain.LeaderboardRecord;
import com.worldmap.ranking.domain.LeaderboardRecordRepository;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
	private PopulationGameSessionRepository populationGameSessionRepository;

	@Autowired
	private CapitalGameSessionRepository capitalGameSessionRepository;

	@Autowired
	private FlagGameSessionRepository flagGameSessionRepository;

	@Autowired
	private PopulationBattleGameSessionRepository populationBattleGameSessionRepository;

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
		String previousSessionId = browserSession.getId();

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
		assertThat(browserSession.getId()).isNotEqualTo(previousSessionId);

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
	void adminLoginRedirectsBackToRequestedDashboardRoute() throws Exception {
		memberRepository.save(
			Member.create(
				"worldmap_admin",
				memberPasswordHasher.hash("secret123"),
				MemberRole.ADMIN
			)
		);

		MockHttpSession browserSession = new MockHttpSession();
		String previousSessionId = browserSession.getId();

		mockMvc.perform(
			post("/login")
				.session(browserSession)
				.param("nickname", "worldmap_admin")
				.param("password", "secret123")
				.param("returnTo", "/dashboard")
		)
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/dashboard"));

		assertThat(browserSession.getId()).isNotEqualTo(previousSessionId);

		mockMvc.perform(get("/dashboard").session(browserSession))
			.andExpect(status().isOk())
			.andExpect(view().name("admin/index"));
	}

	@Test
	void deletedMemberSessionFallsBackToGuestStateOnHomeAndMyPage() throws Exception {
		Member member = memberRepository.save(Member.create("orbit_runner", memberPasswordHasher.hash("secret123"), MemberRole.USER));
		MockHttpSession browserSession = sessionFor(member);

		memberRepository.delete(member);

		mockMvc.perform(get("/").session(browserSession))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString(">로그인<")))
			.andExpect(content().string(containsString(">회원가입<")))
			.andExpect(content().string(not(containsString(">로그아웃<"))))
			.andExpect(content().string(not(containsString("계정으로 기록을 이어서 남기고 있습니다."))));

		mockMvc.perform(get("/mypage").session(browserSession))
			.andExpect(status().isOk())
			.andExpect(view().name("mypage"))
			.andExpect(content().string(containsString("로그인하면 점수와 랭킹 기록을 계속 저장할 수 있습니다.")))
			.andExpect(content().string(not(containsString("로그아웃"))));

		assertThat(browserSession.getAttribute(MEMBER_ID_ATTRIBUTE)).isNull();
		assertThat(browserSession.getAttribute(MEMBER_NICKNAME_ATTRIBUTE)).isNull();
		assertThat(browserSession.getAttribute(MEMBER_ROLE_ATTRIBUTE)).isNull();
	}

	@Test
	void deletedMemberSessionCanOpenLoginPageInsteadOfRedirectingToMyPage() throws Exception {
		Member member = memberRepository.save(Member.create("orbit_runner", memberPasswordHasher.hash("secret123"), MemberRole.USER));
		MockHttpSession browserSession = sessionFor(member);

		memberRepository.delete(member);

		mockMvc.perform(get("/login").session(browserSession))
			.andExpect(status().isOk())
			.andExpect(view().name("auth/login"))
			.andExpect(content().string(containsString("로그인")));
	}

	@ParameterizedTest
	@MethodSource("gameStartPages")
	void deletedMemberSessionFallsBackToGuestPromptOnGameStartPage(String path, String guestPrompt) throws Exception {
		Member member = memberRepository.save(Member.create("orbit_runner", memberPasswordHasher.hash("secret123"), MemberRole.USER));
		MockHttpSession browserSession = sessionFor(member);

		memberRepository.delete(member);

		mockMvc.perform(get(path).session(browserSession))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("게임 시작")))
			.andExpect(content().string(containsString(guestPrompt)))
			.andExpect(content().string(not(containsString("현재 로그인 상태에서는"))))
			.andExpect(content().string(not(containsString(">orbit_runner<"))));
	}

	@Test
	void deletedMemberSessionStartsNewGameAsGuestOwnership() throws Exception {
		Member member = memberRepository.save(Member.create("orbit_runner", memberPasswordHasher.hash("secret123"), MemberRole.USER));
		MockHttpSession browserSession = sessionFor(member);

		memberRepository.delete(member);

		UUID sessionId = UUID.fromString(startLocationGame("fallback_guest", browserSession));
		LocationGameSession locationGameSession = locationGameSessionRepository.findById(sessionId).orElseThrow();

		assertThat(locationGameSession.getMemberId()).isNull();
		assertThat(locationGameSession.getGuestSessionKey()).isNotBlank();
		assertThat(locationGameSession.getPlayerNickname()).isEqualTo("fallback_guest");
		assertThat(browserSession.getAttribute(MEMBER_ID_ATTRIBUTE)).isNull();
		assertThat(browserSession.getAttribute(MEMBER_NICKNAME_ATTRIBUTE)).isNull();
		assertThat(browserSession.getAttribute(MEMBER_ROLE_ATTRIBUTE)).isNull();
	}

	@Test
	void signupClaimsCurrentGuestRecordsIntoMemberOwnership() throws Exception {
		MockHttpSession browserSession = new MockHttpSession();
		UUID guestSessionId = UUID.fromString(startLocationGame("guest_runner", browserSession));
		UUID populationSessionId = UUID.fromString(startPopulationGame("guest_population", browserSession));
		UUID capitalSessionId = UUID.fromString(startCapitalGame("guest_capital", browserSession));
		UUID flagSessionId = UUID.fromString(startFlagGame("guest_flag", browserSession));
		UUID populationBattleSessionId = UUID.fromString(startPopulationBattleGame("guest_battle", browserSession));
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
		PopulationGameSession claimedPopulationSession = populationGameSessionRepository.findById(populationSessionId)
			.orElseThrow();
		CapitalGameSession claimedCapitalSession = capitalGameSessionRepository.findById(capitalSessionId).orElseThrow();
		FlagGameSession claimedFlagSession = flagGameSessionRepository.findById(flagSessionId).orElseThrow();
		PopulationBattleGameSession claimedPopulationBattleSession =
			populationBattleGameSessionRepository.findById(populationBattleSessionId).orElseThrow();
		LeaderboardRecord claimedRecord = leaderboardRecordRepository.findAll().getFirst();

		assertThat(claimedSession.getMemberId()).isEqualTo(member.getId());
		assertThat(claimedSession.getGuestSessionKey()).isNull();
		assertThat(claimedSession.getPlayerNickname()).isEqualTo("guest_runner");
		assertThat(claimedPopulationSession.getMemberId()).isEqualTo(member.getId());
		assertThat(claimedPopulationSession.getGuestSessionKey()).isNull();
		assertThat(claimedPopulationSession.getPlayerNickname()).isEqualTo("guest_population");
		assertThat(claimedCapitalSession.getMemberId()).isEqualTo(member.getId());
		assertThat(claimedCapitalSession.getGuestSessionKey()).isNull();
		assertThat(claimedCapitalSession.getPlayerNickname()).isEqualTo("guest_capital");
		assertThat(claimedFlagSession.getMemberId()).isEqualTo(member.getId());
		assertThat(claimedFlagSession.getGuestSessionKey()).isNull();
		assertThat(claimedFlagSession.getPlayerNickname()).isEqualTo("guest_flag");
		assertThat(claimedPopulationBattleSession.getMemberId()).isEqualTo(member.getId());
		assertThat(claimedPopulationBattleSession.getGuestSessionKey()).isNull();
		assertThat(claimedPopulationBattleSession.getPlayerNickname()).isEqualTo("guest_battle");

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

	@Test
	void loginClaimsCurrentGuestRecordsIntoMemberOwnership() throws Exception {
		memberRepository.save(
			Member.create(
				"claimed_runner",
				memberPasswordHasher.hash("secret1234"),
				MemberRole.USER
			)
		);

		MockHttpSession browserSession = new MockHttpSession();
		UUID locationSessionId = UUID.fromString(startLocationGame("guest_runner", browserSession));
		UUID populationSessionId = UUID.fromString(startPopulationGame("guest_population", browserSession));
		UUID capitalSessionId = UUID.fromString(startCapitalGame("guest_capital", browserSession));
		UUID flagSessionId = UUID.fromString(startFlagGame("guest_flag", browserSession));
		UUID populationBattleSessionId = UUID.fromString(startPopulationBattleGame("guest_battle", browserSession));

		mockMvc.perform(
			post("/login")
				.session(browserSession)
				.param("nickname", "claimed_runner")
				.param("password", "secret1234")
		)
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/mypage"));

		Member member = memberRepository.findByNicknameIgnoreCase("claimed_runner").orElseThrow();
		LocationGameSession claimedLocationSession = locationGameSessionRepository.findById(locationSessionId)
			.orElseThrow();
		PopulationGameSession claimedPopulationSession = populationGameSessionRepository.findById(populationSessionId)
			.orElseThrow();
		CapitalGameSession claimedCapitalSession = capitalGameSessionRepository.findById(capitalSessionId).orElseThrow();
		FlagGameSession claimedFlagSession = flagGameSessionRepository.findById(flagSessionId).orElseThrow();
		PopulationBattleGameSession claimedPopulationBattleSession =
			populationBattleGameSessionRepository.findById(populationBattleSessionId).orElseThrow();

		assertThat(claimedLocationSession.getMemberId()).isEqualTo(member.getId());
		assertThat(claimedLocationSession.getGuestSessionKey()).isNull();
		assertThat(claimedLocationSession.getPlayerNickname()).isEqualTo("guest_runner");
		assertThat(claimedPopulationSession.getMemberId()).isEqualTo(member.getId());
		assertThat(claimedPopulationSession.getGuestSessionKey()).isNull();
		assertThat(claimedPopulationSession.getPlayerNickname()).isEqualTo("guest_population");
		assertThat(claimedCapitalSession.getMemberId()).isEqualTo(member.getId());
		assertThat(claimedCapitalSession.getGuestSessionKey()).isNull();
		assertThat(claimedCapitalSession.getPlayerNickname()).isEqualTo("guest_capital");
		assertThat(claimedFlagSession.getMemberId()).isEqualTo(member.getId());
		assertThat(claimedFlagSession.getGuestSessionKey()).isNull();
		assertThat(claimedFlagSession.getPlayerNickname()).isEqualTo("guest_flag");
		assertThat(claimedPopulationBattleSession.getMemberId()).isEqualTo(member.getId());
		assertThat(claimedPopulationBattleSession.getGuestSessionKey()).isNull();
		assertThat(claimedPopulationBattleSession.getPlayerNickname()).isEqualTo("guest_battle");
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

	private String startPopulationGame(String nickname, MockHttpSession browserSession) throws Exception {
		return com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
			.readTree(
				mockMvc.perform(
					post("/api/games/population/sessions")
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

	private String startCapitalGame(String nickname, MockHttpSession browserSession) throws Exception {
		return com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
			.readTree(
				mockMvc.perform(
					post("/api/games/capital/sessions")
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

	private String startFlagGame(String nickname, MockHttpSession browserSession) throws Exception {
		return com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
			.readTree(
				mockMvc.perform(
					post("/api/games/flag/sessions")
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

	private String startPopulationBattleGame(String nickname, MockHttpSession browserSession) throws Exception {
		return com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
			.readTree(
				mockMvc.perform(
					post("/api/games/population-battle/sessions")
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

	private MockHttpSession sessionFor(Member member) {
		MockHttpSession session = new MockHttpSession();
		session.setAttribute(MEMBER_ID_ATTRIBUTE, member.getId());
		session.setAttribute(MEMBER_NICKNAME_ATTRIBUTE, member.getNickname());
		session.setAttribute(MEMBER_ROLE_ATTRIBUTE, member.getRole().name());
		return session;
	}

	private static Stream<Arguments> gameStartPages() {
		return Stream.of(
			Arguments.of("/games/location/start", "닉네임은 비워 두어도 됩니다."),
			Arguments.of("/games/capital/start", "닉네임은 비워 두어도 됩니다."),
			Arguments.of("/games/population/start", "닉네임은 비워 두어도 됩니다."),
			Arguments.of("/games/flag/start", "닉네임은 비워 두어도 됩니다."),
			Arguments.of("/games/population-battle/start", "닉네임은 비워 두어도 됩니다.")
		);
	}
}
