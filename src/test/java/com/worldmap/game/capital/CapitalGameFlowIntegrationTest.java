package com.worldmap.game.capital;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.worldmap.game.capital.domain.CapitalGameAttemptRepository;
import com.worldmap.game.capital.domain.CapitalGameSessionRepository;
import com.worldmap.game.capital.domain.CapitalGameStage;
import com.worldmap.game.capital.domain.CapitalGameStageRepository;
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
class CapitalGameFlowIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private CapitalGameSessionRepository capitalGameSessionRepository;

	@Autowired
	private CapitalGameStageRepository capitalGameStageRepository;

	@Autowired
	private CapitalGameAttemptRepository capitalGameAttemptRepository;

	@Test
	void capitalGameContinuesBeyondFiveStages() throws Exception {
		UUID sessionId = UUID.fromString(startGame("capital-player"));

		for (int stageNumber = 1; stageNumber <= 7; stageNumber++) {
			MvcResult stateResult = mockMvc.perform(get("/api/games/capital/sessions/{sessionId}/state", sessionId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.stageNumber").value(stageNumber))
				.andExpect(jsonPath("$.options.length()").value(4))
				.andExpect(jsonPath("$.livesRemaining").value(3))
				.andExpect(jsonPath("$.difficultyLabel").isNotEmpty())
				.andReturn();

			assertOptionsUseKoreanCapitalNames(stateResult);

			CapitalGameStage stage = capitalGameStageRepository.findBySessionIdAndStageNumber(sessionId, stageNumber)
				.orElseThrow();

			MvcResult answerResult = mockMvc.perform(
				post("/api/games/capital/sessions/{sessionId}/answer", sessionId)
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

		mockMvc.perform(get("/api/games/capital/sessions/{sessionId}", sessionId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("IN_PROGRESS"))
			.andExpect(jsonPath("$.clearedStageCount").value(7))
			.andExpect(jsonPath("$.totalAttemptCount").value(7))
			.andExpect(jsonPath("$.firstTryClearCount").value(7))
			.andExpect(jsonPath("$.stages.length()").value(8));
	}

	@Test
	void capitalGameReturnsKoreanCapitalNamesInAnswerPayload() throws Exception {
		UUID sessionId = UUID.fromString(startGame("capital-korean"));
		CapitalGameStage firstStage = capitalGameStageRepository.findBySessionIdAndStageNumber(sessionId, 1)
			.orElseThrow();

		MvcResult answerResult = mockMvc.perform(
			post("/api/games/capital/sessions/{sessionId}/answer", sessionId)
				.contentType("application/json")
				.content(answerPayload(1, firstStage.getCorrectOptionNumber()))
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.correct").value(true))
			.andReturn();

		JsonNode answerJson = objectMapper.readTree(answerResult.getResponse().getContentAsString());
		assertThat(answerJson.get("selectedCapitalCity").asText()).matches(".*[가-힣].*");
		assertThat(answerJson.get("correctCapitalCity").asText()).matches(".*[가-힣].*");
	}

	@Test
	void wrongAnswerConsumesLifeAndKeepsSameStage() throws Exception {
		UUID sessionId = UUID.fromString(startGame("capital-life"));
		CapitalGameStage firstStage = capitalGameStageRepository.findBySessionIdAndStageNumber(sessionId, 1)
			.orElseThrow();
		int wrongOptionNumber = findWrongOptionNumber(firstStage.getCorrectOptionNumber());

		mockMvc.perform(
			post("/api/games/capital/sessions/{sessionId}/answer", sessionId)
				.contentType("application/json")
				.content(answerPayload(1, wrongOptionNumber))
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.outcome").value("WRONG"))
			.andExpect(jsonPath("$.gameStatus").value("IN_PROGRESS"))
			.andExpect(jsonPath("$.livesRemaining").value(2))
			.andExpect(jsonPath("$.nextStageNumber").value(1))
			.andExpect(jsonPath("$.clearedStageCount").value(0));

		mockMvc.perform(get("/api/games/capital/sessions/{sessionId}/state", sessionId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.stageNumber").value(1))
			.andExpect(jsonPath("$.livesRemaining").value(2));

		assertThat(capitalGameAttemptRepository.findAllByStageSessionIdOrderByStageStageNumberAscAttemptNumberAsc(sessionId))
			.hasSize(1);
	}

	@Test
	void threeWrongAnswersLeadToGameOver() throws Exception {
		UUID sessionId = UUID.fromString(startGame("capital-game-over"));
		CapitalGameStage firstStage = capitalGameStageRepository.findBySessionIdAndStageNumber(sessionId, 1)
			.orElseThrow();
		int wrongOptionNumber = findWrongOptionNumber(firstStage.getCorrectOptionNumber());

		for (int attempt = 1; attempt <= 2; attempt++) {
			mockMvc.perform(
				post("/api/games/capital/sessions/{sessionId}/answer", sessionId)
					.contentType("application/json")
					.content(answerPayload(1, wrongOptionNumber))
			)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.outcome").value("WRONG"));
		}

		mockMvc.perform(
			post("/api/games/capital/sessions/{sessionId}/answer", sessionId)
				.contentType("application/json")
				.content(answerPayload(1, wrongOptionNumber))
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.outcome").value("GAME_OVER"))
			.andExpect(jsonPath("$.gameStatus").value("GAME_OVER"))
			.andExpect(jsonPath("$.livesRemaining").value(0))
			.andExpect(jsonPath("$.nextStageNumber").doesNotExist());

		mockMvc.perform(get("/api/games/capital/sessions/{sessionId}", sessionId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("GAME_OVER"))
			.andExpect(jsonPath("$.livesRemaining").value(0))
			.andExpect(jsonPath("$.stages[0].status").value("FAILED"))
			.andExpect(jsonPath("$.stages[0].attempts.length()").value(3));
	}

	@Test
	void restartReusesSameSessionAndResetsProgress() throws Exception {
		UUID sessionId = UUID.fromString(startGame("capital-restart"));
		CapitalGameStage firstStage = capitalGameStageRepository.findBySessionIdAndStageNumber(sessionId, 1)
			.orElseThrow();
		int wrongOptionNumber = findWrongOptionNumber(firstStage.getCorrectOptionNumber());

		for (int attempt = 1; attempt <= 3; attempt++) {
			mockMvc.perform(
				post("/api/games/capital/sessions/{sessionId}/answer", sessionId)
					.contentType("application/json")
					.content(answerPayload(1, wrongOptionNumber))
			)
				.andExpect(status().isOk());
		}

		mockMvc.perform(post("/api/games/capital/sessions/{sessionId}/restart", sessionId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.sessionId").value(sessionId.toString()))
			.andExpect(jsonPath("$.status").value("IN_PROGRESS"))
			.andExpect(jsonPath("$.livesRemaining").value(3))
			.andExpect(jsonPath("$.totalStages").value(1))
			.andExpect(jsonPath("$.playPageUrl").value("/games/capital/play/" + sessionId));

		mockMvc.perform(get("/api/games/capital/sessions/{sessionId}/state", sessionId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.stageNumber").value(1))
			.andExpect(jsonPath("$.livesRemaining").value(3))
			.andExpect(jsonPath("$.clearedStageCount").value(0))
			.andExpect(jsonPath("$.totalScore").value(0));

		assertThat(capitalGameAttemptRepository.findAllByStageSessionIdOrderByStageStageNumberAscAttemptNumberAsc(sessionId))
			.isEmpty();
		assertThat(capitalGameStageRepository.findAllBySessionIdOrderByStageNumber(sessionId))
			.hasSize(1);
	}

	private String startGame(String nickname) throws Exception {
		MvcResult result = mockMvc.perform(
			post("/api/games/capital/sessions")
				.contentType("application/json")
				.content("{\"nickname\":\"" + nickname + "\"}")
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

	private int findWrongOptionNumber(int correctOptionNumber) {
		return correctOptionNumber == 1 ? 2 : 1;
	}

	private void assertOptionsUseKoreanCapitalNames(MvcResult stateResult) throws Exception {
		JsonNode stateJson = objectMapper.readTree(stateResult.getResponse().getContentAsString());
		for (JsonNode option : stateJson.get("options")) {
			assertThat(option.get("capitalCity").asText()).matches(".*[가-힣].*");
		}
	}
}
