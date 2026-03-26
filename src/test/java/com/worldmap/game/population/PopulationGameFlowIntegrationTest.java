package com.worldmap.game.population;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.worldmap.game.population.domain.PopulationGameAttemptRepository;
import com.worldmap.game.population.domain.PopulationGameSessionRepository;
import com.worldmap.game.population.domain.PopulationGameStage;
import com.worldmap.game.population.domain.PopulationGameStageRepository;
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
class PopulationGameFlowIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private PopulationGameSessionRepository populationGameSessionRepository;

	@Autowired
	private PopulationGameStageRepository populationGameStageRepository;

	@Autowired
	private PopulationGameAttemptRepository populationGameAttemptRepository;

	@Test
	void populationGameContinuesBeyondFiveStages() throws Exception {
		UUID sessionId = UUID.fromString(startGame("population-player"));

		for (int stageNumber = 1; stageNumber <= 7; stageNumber++) {
			mockMvc.perform(get("/api/games/population/sessions/{sessionId}/state", sessionId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.gameLevel").value("LEVEL_1"))
				.andExpect(jsonPath("$.stageNumber").value(stageNumber))
				.andExpect(jsonPath("$.options", hasSize(4)))
				.andExpect(jsonPath("$.livesRemaining").value(3))
				.andExpect(jsonPath("$.difficultyLabel").isNotEmpty());

			PopulationGameStage stage = populationGameStageRepository.findBySessionIdAndStageNumber(sessionId, stageNumber)
				.orElseThrow();

			MvcResult answerResult = mockMvc.perform(
				post("/api/games/population/sessions/{sessionId}/answer", sessionId)
					.contentType("application/json")
					.content(answerPayload(stageNumber, stage.getCorrectOptionNumber()))
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

		mockMvc.perform(get("/api/games/population/sessions/{sessionId}", sessionId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("IN_PROGRESS"))
			.andExpect(jsonPath("$.clearedStageCount").value(7))
			.andExpect(jsonPath("$.totalAttemptCount").value(7))
			.andExpect(jsonPath("$.firstTryClearCount").value(7))
			.andExpect(jsonPath("$.stages", hasSize(8)));
	}

	@Test
	void wrongAnswerConsumesLifeAndKeepsSameStage() throws Exception {
		UUID sessionId = UUID.fromString(startGame("pop-life"));
		PopulationGameStage firstStage = populationGameStageRepository.findBySessionIdAndStageNumber(sessionId, 1)
			.orElseThrow();
		int wrongOptionNumber = findWrongOptionNumber(firstStage.getCorrectOptionNumber());

		mockMvc.perform(
			post("/api/games/population/sessions/{sessionId}/answer", sessionId)
				.contentType("application/json")
				.content(answerPayload(1, wrongOptionNumber))
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.gameLevel").value("LEVEL_1"))
			.andExpect(jsonPath("$.outcome").value("WRONG"))
			.andExpect(jsonPath("$.gameStatus").value("IN_PROGRESS"))
			.andExpect(jsonPath("$.livesRemaining").value(2))
			.andExpect(jsonPath("$.nextStageNumber").value(1))
			.andExpect(jsonPath("$.clearedStageCount").value(0));

		mockMvc.perform(get("/api/games/population/sessions/{sessionId}/state", sessionId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.stageNumber").value(1))
			.andExpect(jsonPath("$.livesRemaining").value(2));

		assertThat(populationGameAttemptRepository.findAllByStageSessionIdOrderByStageStageNumberAscAttemptNumberAsc(sessionId))
			.hasSize(1);
	}

	@Test
	void threeWrongAnswersLeadToGameOver() throws Exception {
		UUID sessionId = UUID.fromString(startGame("population-game-over"));
		PopulationGameStage firstStage = populationGameStageRepository.findBySessionIdAndStageNumber(sessionId, 1)
			.orElseThrow();
		int wrongOptionNumber = findWrongOptionNumber(firstStage.getCorrectOptionNumber());

		for (int attempt = 1; attempt <= 2; attempt++) {
			mockMvc.perform(
				post("/api/games/population/sessions/{sessionId}/answer", sessionId)
					.contentType("application/json")
					.content(answerPayload(1, wrongOptionNumber))
			)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.outcome").value("WRONG"));
		}

		mockMvc.perform(
			post("/api/games/population/sessions/{sessionId}/answer", sessionId)
				.contentType("application/json")
				.content(answerPayload(1, wrongOptionNumber))
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.outcome").value("GAME_OVER"))
			.andExpect(jsonPath("$.gameStatus").value("GAME_OVER"))
			.andExpect(jsonPath("$.livesRemaining").value(0))
			.andExpect(jsonPath("$.nextStageNumber").doesNotExist());

		mockMvc.perform(get("/api/games/population/sessions/{sessionId}", sessionId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("GAME_OVER"))
			.andExpect(jsonPath("$.livesRemaining").value(0))
			.andExpect(jsonPath("$.stages[0].status").value("FAILED"))
			.andExpect(jsonPath("$.stages[0].attempts", hasSize(3)));
	}

	@Test
	void levelTwoUsesExactPopulationInputAndReturnsErrorRate() throws Exception {
		UUID sessionId = UUID.fromString(startGame("population-l2", "LEVEL_2"));
		PopulationGameStage firstStage = populationGameStageRepository.findBySessionIdAndStageNumber(sessionId, 1)
			.orElseThrow();

		mockMvc.perform(get("/api/games/population/sessions/{sessionId}/state", sessionId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.gameLevel").value("LEVEL_2"))
			.andExpect(jsonPath("$.options", hasSize(0)));

		mockMvc.perform(
			post("/api/games/population/sessions/{sessionId}/answer", sessionId)
				.contentType("application/json")
				.content(exactAnswerPayload(1, firstStage.getTargetPopulation()))
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.gameLevel").value("LEVEL_2"))
			.andExpect(jsonPath("$.correct").value(true))
			.andExpect(jsonPath("$.errorRatePercent").value(0.0))
			.andExpect(jsonPath("$.precisionBand").value("PRECISE_HIT"))
			.andExpect(jsonPath("$.selectedOptionNumber").doesNotExist())
			.andExpect(jsonPath("$.correctOptionNumber").doesNotExist())
			.andExpect(jsonPath("$.selectedOptionLabel").exists())
			.andExpect(jsonPath("$.nextStageNumber").value(2));
	}

	@Test
	void levelTwoFarWrongInputConsumesLife() throws Exception {
		UUID sessionId = UUID.fromString(startGame("population-l2-life", "LEVEL_2"));

		mockMvc.perform(
			post("/api/games/population/sessions/{sessionId}/answer", sessionId)
				.contentType("application/json")
				.content(exactAnswerPayload(1, 1L))
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.gameLevel").value("LEVEL_2"))
			.andExpect(jsonPath("$.correct").value(false))
			.andExpect(jsonPath("$.outcome").value("WRONG"))
			.andExpect(jsonPath("$.livesRemaining").value(2))
			.andExpect(jsonPath("$.errorRatePercent").isNumber())
			.andExpect(jsonPath("$.precisionBand").value("MISS"));
	}

	@Test
	void levelTwoResultPageShowsPrecisionGuideAndAttemptBands() throws Exception {
		UUID sessionId = UUID.fromString(startGame("population-l2-result", "LEVEL_2"));
		PopulationGameStage firstStage = populationGameStageRepository.findBySessionIdAndStageNumber(sessionId, 1)
			.orElseThrow();

		mockMvc.perform(
			post("/api/games/population/sessions/{sessionId}/answer", sessionId)
				.contentType("application/json")
				.content(exactAnswerPayload(1, firstStage.getTargetPopulation()))
		)
			.andExpect(status().isOk());

		for (int attempt = 1; attempt <= 3; attempt++) {
			mockMvc.perform(
				post("/api/games/population/sessions/{sessionId}/answer", sessionId)
					.contentType("application/json")
					.content(exactAnswerPayload(2, 1L))
			)
				.andExpect(status().isOk());
		}

		mockMvc.perform(get("/games/population/result/{sessionId}", sessionId))
			.andExpect(status().isOk())
			.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.view().name("population-game/result"))
			.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(containsString("Level 2 판정 기준")))
			.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(containsString("정밀 적중")))
			.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(containsString("오차율")))
			.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(containsString("허용 범위 정답")));
	}

	@Test
	void restartReusesSameSessionAndResetsProgress() throws Exception {
		UUID sessionId = UUID.fromString(startGame("population-restart"));
		PopulationGameStage firstStage = populationGameStageRepository.findBySessionIdAndStageNumber(sessionId, 1)
			.orElseThrow();
		int wrongOptionNumber = findWrongOptionNumber(firstStage.getCorrectOptionNumber());

		for (int attempt = 1; attempt <= 3; attempt++) {
			mockMvc.perform(
				post("/api/games/population/sessions/{sessionId}/answer", sessionId)
					.contentType("application/json")
					.content(answerPayload(1, wrongOptionNumber))
			)
				.andExpect(status().isOk());
		}

		mockMvc.perform(post("/api/games/population/sessions/{sessionId}/restart", sessionId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.sessionId").value(sessionId.toString()))
			.andExpect(jsonPath("$.status").value("IN_PROGRESS"))
			.andExpect(jsonPath("$.livesRemaining").value(3))
			.andExpect(jsonPath("$.totalStages").value(1))
			.andExpect(jsonPath("$.playPageUrl").value("/games/population/play/" + sessionId));

		mockMvc.perform(get("/api/games/population/sessions/{sessionId}/state", sessionId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.stageNumber").value(1))
			.andExpect(jsonPath("$.livesRemaining").value(3))
			.andExpect(jsonPath("$.clearedStageCount").value(0))
			.andExpect(jsonPath("$.totalScore").value(0));

		assertThat(populationGameAttemptRepository.findAllByStageSessionIdOrderByStageStageNumberAscAttemptNumberAsc(sessionId))
			.isEmpty();
		assertThat(populationGameStageRepository.findAllBySessionIdOrderByStageNumber(sessionId))
			.hasSize(1);
	}

	private String startGame(String nickname) throws Exception {
		return startGame(nickname, "LEVEL_1");
	}

	private String startGame(String nickname, String gameLevel) throws Exception {
		MvcResult result = mockMvc.perform(
			post("/api/games/population/sessions")
				.contentType("application/json")
				.content("{\"nickname\":\"" + nickname + "\",\"gameLevel\":\"" + gameLevel + "\"}")
		)
			.andExpect(status().isCreated())
			.andReturn();

		JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
		return json.get("sessionId").asText();
	}

	private String answerPayload(Integer stageNumber, Integer selectedOptionNumber) {
		return """
			{
			  "stageNumber": %d,
			  "selectedOptionNumber": %d
			}
			""".formatted(stageNumber, selectedOptionNumber);
	}

	private String exactAnswerPayload(Integer stageNumber, Long submittedPopulation) {
		return """
			{
			  "stageNumber": %d,
			  "submittedPopulation": %d
			}
			""".formatted(stageNumber, submittedPopulation);
	}

	private int findWrongOptionNumber(int correctOptionNumber) {
		return correctOptionNumber == 1 ? 2 : 1;
	}
}
