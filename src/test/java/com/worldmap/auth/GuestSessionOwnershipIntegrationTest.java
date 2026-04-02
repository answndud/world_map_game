package com.worldmap.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.worldmap.country.domain.CountryRepository;
import com.worldmap.auth.domain.Member;
import com.worldmap.auth.domain.MemberRepository;
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
import jakarta.servlet.http.HttpSession;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GuestSessionOwnershipIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private LocationGameSessionRepository locationGameSessionRepository;

	@Autowired
	private PopulationGameSessionRepository populationGameSessionRepository;

	@Autowired
	private CapitalGameSessionRepository capitalGameSessionRepository;

	@Autowired
	private FlagGameSessionRepository flagGameSessionRepository;

	@Autowired
	private PopulationBattleGameSessionRepository populationBattleGameSessionRepository;

	@Autowired
	private LocationGameStageRepository locationGameStageRepository;

	@Autowired
	private LeaderboardRecordRepository leaderboardRecordRepository;

	@Autowired
	private CountryRepository countryRepository;

	@Autowired
	private MemberRepository memberRepository;

	@BeforeEach
	void clearLeaderboardRecords() {
		leaderboardRecordRepository.deleteAll();
		memberRepository.deleteAll();
	}

	@Test
	void sameBrowserSessionSharesGuestSessionKeyAcrossGameModes() throws Exception {
		MockHttpSession browserSession = new MockHttpSession();

		UUID locationSessionId = UUID.fromString(startLocationGame("guest-location", browserSession));
		UUID populationSessionId = UUID.fromString(startPopulationGame("guest-population", browserSession));
		UUID capitalSessionId = UUID.fromString(startCapitalGame("guest-capital", browserSession));
		UUID flagSessionId = UUID.fromString(startFlagGame("guest-flag", browserSession));
		UUID populationBattleSessionId = UUID.fromString(startPopulationBattleGame("guest-battle", browserSession));

		LocationGameSession locationGameSession = locationGameSessionRepository.findById(locationSessionId).orElseThrow();
		PopulationGameSession populationGameSession = populationGameSessionRepository.findById(populationSessionId).orElseThrow();
		CapitalGameSession capitalGameSession = capitalGameSessionRepository.findById(capitalSessionId).orElseThrow();
		FlagGameSession flagGameSession = flagGameSessionRepository.findById(flagSessionId).orElseThrow();
		PopulationBattleGameSession populationBattleGameSession =
			populationBattleGameSessionRepository.findById(populationBattleSessionId).orElseThrow();

		assertThat(locationGameSession.getGuestSessionKey()).isNotBlank();
		assertThat(locationGameSession.getGuestSessionKey()).isEqualTo(populationGameSession.getGuestSessionKey());
		assertThat(locationGameSession.getGuestSessionKey()).isEqualTo(capitalGameSession.getGuestSessionKey());
		assertThat(locationGameSession.getGuestSessionKey()).isEqualTo(flagGameSession.getGuestSessionKey());
		assertThat(locationGameSession.getGuestSessionKey()).isEqualTo(populationBattleGameSession.getGuestSessionKey());
		assertThat(locationGameSession.getMemberId()).isNull();
		assertThat(populationGameSession.getMemberId()).isNull();
		assertThat(capitalGameSession.getMemberId()).isNull();
		assertThat(flagGameSession.getMemberId()).isNull();
		assertThat(populationBattleGameSession.getMemberId()).isNull();
	}

	@Test
	void leaderboardRecordKeepsGuestOwnershipWhenGuestGameEnds() throws Exception {
		MockHttpSession browserSession = new MockHttpSession();
		UUID sessionId = UUID.fromString(startLocationGame("guest-ranker", browserSession));
		LocationGameSession locationSession = locationGameSessionRepository.findById(sessionId).orElseThrow();

		LocationGameStage firstStage = locationGameStageRepository.findBySessionIdAndStageNumber(sessionId, 1)
			.orElseThrow();
		String wrongCountryIso3Code = countryRepository.findAll().stream()
			.map(country -> country.getIso3Code())
			.filter(iso3Code -> !iso3Code.equals(firstStage.getTargetCountryIso3Code()))
			.findFirst()
			.orElseThrow();

		for (int attempt = 1; attempt <= 3; attempt++) {
			mockMvc.perform(
				post("/api/games/location/sessions/{sessionId}/answer", sessionId)
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

		LeaderboardRecord leaderboardRecord = leaderboardRecordRepository.findAll().getFirst();
		assertThat(leaderboardRecord.getMemberId()).isNull();
		assertThat(leaderboardRecord.getGuestSessionKey()).isEqualTo(locationSession.getGuestSessionKey());
		assertThat(leaderboardRecord.getPlayerNickname()).isEqualTo("guest-ranker");
	}

	@Test
	void loggedInMemberStartsGamesWithMemberOwnership() throws Exception {
		MockHttpSession browserSession = new MockHttpSession();

		mockMvc.perform(
			post("/signup")
				.session(browserSession)
				.param("nickname", "member_runner")
				.param("password", "secret1234")
		)
			.andExpect(status().is3xxRedirection());

		Member member = memberRepository.findByNicknameIgnoreCase("member_runner").orElseThrow();
		UUID sessionId = UUID.fromString(startLocationGame("ignored_guest_name", browserSession));
		LocationGameSession locationSession = locationGameSessionRepository.findById(sessionId).orElseThrow();

		assertThat(locationSession.getMemberId()).isEqualTo(member.getId());
		assertThat(locationSession.getGuestSessionKey()).isNull();
		assertThat(locationSession.getPlayerNickname()).isEqualTo("member_runner");

		LocationGameStage firstStage = locationGameStageRepository.findBySessionIdAndStageNumber(sessionId, 1)
			.orElseThrow();
		String wrongCountryIso3Code = countryRepository.findAll().stream()
			.map(country -> country.getIso3Code())
			.filter(iso3Code -> !iso3Code.equals(firstStage.getTargetCountryIso3Code()))
			.findFirst()
			.orElseThrow();

		for (int attempt = 1; attempt <= 3; attempt++) {
			mockMvc.perform(
				post("/api/games/location/sessions/{sessionId}/answer", sessionId)
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

		LeaderboardRecord leaderboardRecord = leaderboardRecordRepository.findAll().getFirst();
		assertThat(leaderboardRecord.getMemberId()).isEqualTo(member.getId());
		assertThat(leaderboardRecord.getGuestSessionKey()).isNull();
		assertThat(leaderboardRecord.getPlayerNickname()).isEqualTo("member_runner");
	}

	private String startLocationGame(String nickname, HttpSession browserSession) throws Exception {
		MvcResult result = mockMvc.perform(
			post("/api/games/location/sessions")
				.session((MockHttpSession) browserSession)
				.contentType("application/json")
				.content("{\"nickname\":\"" + nickname + "\"}")
		)
			.andExpect(status().isCreated())
			.andReturn();

		JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
		return json.get("sessionId").asText();
	}

	private String startPopulationGame(String nickname, HttpSession browserSession) throws Exception {
		MvcResult result = mockMvc.perform(
			post("/api/games/population/sessions")
				.session((MockHttpSession) browserSession)
				.contentType("application/json")
				.content("{\"nickname\":\"" + nickname + "\"}")
		)
			.andExpect(status().isCreated())
			.andReturn();

		JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
		return json.get("sessionId").asText();
	}

	private String startCapitalGame(String nickname, HttpSession browserSession) throws Exception {
		MvcResult result = mockMvc.perform(
			post("/api/games/capital/sessions")
				.session((MockHttpSession) browserSession)
				.contentType("application/json")
				.content("{\"nickname\":\"" + nickname + "\"}")
		)
			.andExpect(status().isCreated())
			.andReturn();

		JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
		return json.get("sessionId").asText();
	}

	private String startFlagGame(String nickname, HttpSession browserSession) throws Exception {
		MvcResult result = mockMvc.perform(
			post("/api/games/flag/sessions")
				.session((MockHttpSession) browserSession)
				.contentType("application/json")
				.content("{\"nickname\":\"" + nickname + "\"}")
		)
			.andExpect(status().isCreated())
			.andReturn();

		JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
		return json.get("sessionId").asText();
	}

	private String startPopulationBattleGame(String nickname, HttpSession browserSession) throws Exception {
		MvcResult result = mockMvc.perform(
			post("/api/games/population-battle/sessions")
				.session((MockHttpSession) browserSession)
				.contentType("application/json")
				.content("{\"nickname\":\"" + nickname + "\"}")
		)
			.andExpect(status().isCreated())
			.andReturn();

		JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
		return json.get("sessionId").asText();
	}
}
