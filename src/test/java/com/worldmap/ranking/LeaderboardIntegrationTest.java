package com.worldmap.ranking;

import static org.hamcrest.Matchers.not;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.worldmap.country.domain.CountryRepository;
import com.worldmap.game.location.domain.LocationGameStage;
import com.worldmap.game.location.domain.LocationGameStageRepository;
import com.worldmap.game.population.domain.PopulationGameStage;
import com.worldmap.game.population.domain.PopulationGameStageRepository;
import com.worldmap.ranking.domain.LeaderboardRecordRepository;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LeaderboardIntegrationTest {

	private static final String TEST_PREFIX = "test:leaderboard";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	@Autowired
	private LeaderboardRecordRepository leaderboardRecordRepository;

	@Autowired
	private LocationGameStageRepository locationGameStageRepository;

	@Autowired
	private PopulationGameStageRepository populationGameStageRepository;

	@Autowired
	private CountryRepository countryRepository;

	@BeforeEach
	void clearLeaderboardState() {
		leaderboardRecordRepository.deleteAll();
		Set<String> keys = stringRedisTemplate.keys(TEST_PREFIX + "*");
		if (keys != null && !keys.isEmpty()) {
			stringRedisTemplate.delete(keys);
		}
	}

	@Test
	void gameOverRecordsLocationLeaderboardAndRendersRankingPage() throws Exception {
		UUID sessionId = UUID.fromString(startLocationGame("rank-location", "LEVEL_1"));

		LocationGameStage firstStage = locationGameStageRepository.findBySessionIdAndStageNumber(sessionId, 1)
			.orElseThrow();

		MvcResult correctAnswerResult = mockMvc.perform(
			post("/api/games/location/sessions/{sessionId}/answer", sessionId)
				.contentType("application/json")
				.content(locationAnswerPayload(1, firstStage.getTargetCountryIso3Code()))
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.correct").value(true))
			.andReturn();

		int totalScore = objectMapper.readTree(correctAnswerResult.getResponse().getContentAsString())
			.get("totalScore")
			.asInt();

		LocationGameStage secondStage = locationGameStageRepository.findBySessionIdAndStageNumber(sessionId, 2)
			.orElseThrow();
		String wrongCountryIso3Code = wrongCountryIso3Code(secondStage.getTargetCountryIso3Code());

		for (int attempt = 1; attempt <= 3; attempt++) {
			mockMvc.perform(
				post("/api/games/location/sessions/{sessionId}/answer", sessionId)
					.contentType("application/json")
					.content(locationAnswerPayload(2, wrongCountryIso3Code))
			)
				.andExpect(status().isOk());
		}

		mockMvc.perform(get("/api/rankings/location").param("scope", "ALL").param("limit", "5"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.gameMode").value("LOCATION"))
			.andExpect(jsonPath("$.entries[0].playerNickname").value("rank-location"))
			.andExpect(jsonPath("$.entries[0].totalScore").value(totalScore))
			.andExpect(jsonPath("$.entries[0].clearedStageCount").value(1))
			.andExpect(jsonPath("$.entries[0].totalAttemptCount").value(4));

		mockMvc.perform(get("/api/rankings/location").param("scope", "DAILY").param("limit", "5"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.scope").value("DAILY"))
			.andExpect(jsonPath("$.entries[0].playerNickname").value("rank-location"));

			mockMvc.perform(get("/ranking"))
				.andExpect(status().isOk())
				.andExpect(view().name("ranking/index"))
				.andExpect(content().string(containsString("지금 새로고침")))
				.andExpect(content().string(containsString("게임 종류")))
				.andExpect(content().string(containsString("동점 처리")))
				.andExpect(content().string(containsString("15초마다 갱신")))
				.andExpect(content().string(not(containsString("Redis Leaderboard"))))
			.andExpect(model().attributeExists("locationAll"))
			.andExpect(model().attributeExists("populationAll"));

		assertThat(leaderboardRecordRepository.count()).isEqualTo(1);
		assertThat(stringRedisTemplate.opsForZSet().zCard(TEST_PREFIX + ":all:location:l1")).isEqualTo(1L);
	}

	private String startLocationGame(String nickname) throws Exception {
		return startLocationGame(nickname, "LEVEL_1");
	}

	private String startLocationGame(String nickname, String gameLevel) throws Exception {
		MvcResult result = mockMvc.perform(
			post("/api/games/location/sessions")
				.contentType("application/json")
				.content("{\"nickname\":\"" + nickname + "\",\"gameLevel\":\"" + gameLevel + "\"}")
		)
			.andExpect(status().isCreated())
			.andReturn();

		JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
		return json.get("sessionId").asText();
	}

	private String startPopulationGame(String nickname) throws Exception {
		MvcResult result = mockMvc.perform(
			post("/api/games/population/sessions")
				.contentType("application/json")
				.content("{\"nickname\":\"" + nickname + "\"}")
		)
			.andExpect(status().isCreated())
			.andReturn();

		JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
		return json.get("sessionId").asText();
	}

	private String locationAnswerPayload(Integer stageNumber, String selectedCountryIso3Code) {
		return """
			{
			  "stageNumber": %d,
			  "selectedCountryIso3Code": "%s"
			}
			""".formatted(stageNumber, selectedCountryIso3Code);
	}

	private String populationAnswerPayload(Integer stageNumber, Integer selectedOptionNumber) {
		return """
			{
			  "stageNumber": %d,
			  "selectedOptionNumber": %d
			}
			""".formatted(stageNumber, selectedOptionNumber);
	}

	private String wrongCountryIso3Code(String targetCountryIso3Code) {
		return countryRepository.findAll().stream()
			.map(country -> country.getIso3Code())
			.filter(iso3Code -> !iso3Code.equals(targetCountryIso3Code))
			.findFirst()
			.orElseThrow();
	}
}
