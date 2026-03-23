package com.worldmap.game.location;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.worldmap.country.domain.CountryRepository;
import com.worldmap.game.location.domain.LocationGameAttemptRepository;
import com.worldmap.game.location.domain.LocationGameStage;
import com.worldmap.game.location.domain.LocationGameStageRepository;
import com.worldmap.game.location.domain.LocationGameSessionRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LocationGameFlowIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private LocationGameSessionRepository locationGameSessionRepository;

	@Autowired
	private LocationGameStageRepository locationGameStageRepository;

	@Autowired
	private LocationGameAttemptRepository locationGameAttemptRepository;

	@Autowired
	private CountryRepository countryRepository;

	@Test
	void locationGameContinuesBeyondFiveStages() throws Exception {
		UUID sessionId = UUID.fromString(startGame("geo-player"));

		for (int stageNumber = 1; stageNumber <= 7; stageNumber++) {
			mockMvc.perform(get("/api/games/location/sessions/{sessionId}/state", sessionId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.stageNumber").value(stageNumber))
				.andExpect(jsonPath("$.livesRemaining").value(3))
				.andExpect(jsonPath("$.difficultyLabel").isNotEmpty());

			LocationGameStage stage = locationGameStageRepository.findBySessionIdAndStageNumber(sessionId, stageNumber)
				.orElseThrow();

			MvcResult answerResult = mockMvc.perform(
				post("/api/games/location/sessions/{sessionId}/answer", sessionId)
					.contentType("application/json")
					.content(answerPayload(stageNumber, stage.getTargetCountryIso3Code()))
			)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.stageNumber").value(stageNumber))
				.andExpect(jsonPath("$.correct").value(true))
				.andExpect(jsonPath("$.outcome").value("CORRECT"))
				.andExpect(jsonPath("$.nextStageNumber").value(stageNumber + 1))
				.andReturn();

			JsonNode answerJson = objectMapper.readTree(answerResult.getResponse().getContentAsString());
			assertThat(answerJson.get("gameStatus").asText()).isEqualTo("IN_PROGRESS");
		}

		mockMvc.perform(get("/api/games/location/sessions/{sessionId}/state", sessionId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.stageNumber").value(8))
			.andExpect(jsonPath("$.clearedStageCount").value(7));

		mockMvc.perform(get("/api/games/location/sessions/{sessionId}/result", sessionId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("IN_PROGRESS"))
			.andExpect(jsonPath("$.clearedStageCount").value(7))
			.andExpect(jsonPath("$.stages", hasSize(8)));
	}

	@Test
	void wrongAnswerConsumesLifeAndKeepsSameStage() throws Exception {
		UUID sessionId = UUID.fromString(startGame("life-check"));
		LocationGameStage firstStage = locationGameStageRepository.findBySessionIdAndStageNumber(sessionId, 1)
			.orElseThrow();
		String wrongCountryIso3Code = findWrongCountryIso3Code(sessionId, firstStage.getTargetCountryIso3Code());

		mockMvc.perform(
			post("/api/games/location/sessions/{sessionId}/answer", sessionId)
				.contentType("application/json")
				.content(answerPayload(1, wrongCountryIso3Code))
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.outcome").value("WRONG"))
			.andExpect(jsonPath("$.gameStatus").value("IN_PROGRESS"))
			.andExpect(jsonPath("$.livesRemaining").value(2))
			.andExpect(jsonPath("$.nextStageNumber").value(1))
			.andExpect(jsonPath("$.clearedStageCount").value(0));

		mockMvc.perform(get("/api/games/location/sessions/{sessionId}/state", sessionId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.stageNumber").value(1))
			.andExpect(jsonPath("$.livesRemaining").value(2));

		assertThat(locationGameAttemptRepository.findAllByStageSessionIdOrderByStageStageNumberAscAttemptNumberAsc(sessionId))
			.hasSize(1);
	}

	@Test
	void threeWrongAnswersLeadToGameOver() throws Exception {
		UUID sessionId = UUID.fromString(startGame("game-over"));
		LocationGameStage firstStage = locationGameStageRepository.findBySessionIdAndStageNumber(sessionId, 1)
			.orElseThrow();
		String wrongCountryIso3Code = findWrongCountryIso3Code(sessionId, firstStage.getTargetCountryIso3Code());

		for (int attempt = 1; attempt <= 2; attempt++) {
			mockMvc.perform(
				post("/api/games/location/sessions/{sessionId}/answer", sessionId)
					.contentType("application/json")
					.content(answerPayload(1, wrongCountryIso3Code))
			)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.outcome").value("WRONG"));
		}

		mockMvc.perform(
			post("/api/games/location/sessions/{sessionId}/answer", sessionId)
				.contentType("application/json")
				.content(answerPayload(1, wrongCountryIso3Code))
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.outcome").value("GAME_OVER"))
			.andExpect(jsonPath("$.gameStatus").value("GAME_OVER"))
			.andExpect(jsonPath("$.livesRemaining").value(0))
			.andExpect(jsonPath("$.nextStageNumber").doesNotExist());

		mockMvc.perform(get("/api/games/location/sessions/{sessionId}/result", sessionId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("GAME_OVER"))
			.andExpect(jsonPath("$.livesRemaining").value(0))
			.andExpect(jsonPath("$.stages[0].status").value("FAILED"))
			.andExpect(jsonPath("$.stages[0].attempts", hasSize(3)));
	}

	@Test
	void restartReusesSameSessionAndResetsProgress() throws Exception {
		UUID sessionId = UUID.fromString(startGame("restart-check"));
		LocationGameStage firstStage = locationGameStageRepository.findBySessionIdAndStageNumber(sessionId, 1)
			.orElseThrow();
		String wrongCountryIso3Code = findWrongCountryIso3Code(sessionId, firstStage.getTargetCountryIso3Code());

		for (int attempt = 1; attempt <= 3; attempt++) {
			mockMvc.perform(
				post("/api/games/location/sessions/{sessionId}/answer", sessionId)
					.contentType("application/json")
					.content(answerPayload(1, wrongCountryIso3Code))
			)
				.andExpect(status().isOk());
		}

		mockMvc.perform(post("/api/games/location/sessions/{sessionId}/restart", sessionId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.sessionId").value(sessionId.toString()))
			.andExpect(jsonPath("$.status").value("IN_PROGRESS"))
			.andExpect(jsonPath("$.livesRemaining").value(3))
			.andExpect(jsonPath("$.totalStages").value(1))
			.andExpect(jsonPath("$.playPageUrl").value("/games/location/play/" + sessionId));

		mockMvc.perform(get("/api/games/location/sessions/{sessionId}/state", sessionId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.stageNumber").value(1))
			.andExpect(jsonPath("$.livesRemaining").value(3))
			.andExpect(jsonPath("$.clearedStageCount").value(0))
			.andExpect(jsonPath("$.totalScore").value(0));

		assertThat(locationGameAttemptRepository.findAllByStageSessionIdOrderByStageStageNumberAscAttemptNumberAsc(sessionId))
			.isEmpty();
		assertThat(locationGameStageRepository.findAllBySessionIdOrderByStageNumber(sessionId))
			.hasSize(1);
	}

	private String startGame(String nickname) throws Exception {
		MvcResult result = mockMvc.perform(
			post("/api/games/location/sessions")
				.contentType("application/json")
				.content("{\"nickname\":\"" + nickname + "\"}")
		)
			.andExpect(status().isCreated())
			.andReturn();

		JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
		return json.get("sessionId").asText();
	}

	private String answerPayload(Integer stageNumber, String selectedCountryIso3Code) {
		return """
			{
			  "stageNumber": %d,
			  "selectedCountryIso3Code": "%s"
			}
			""".formatted(stageNumber, selectedCountryIso3Code);
	}

	private String findWrongCountryIso3Code(UUID sessionId, String targetCountryIso3Code) {
		return countryRepository.findAll().stream()
			.map(country -> country.getIso3Code())
			.filter(iso3Code -> !iso3Code.equals(targetCountryIso3Code))
			.findFirst()
			.orElseThrow();
	}
}
