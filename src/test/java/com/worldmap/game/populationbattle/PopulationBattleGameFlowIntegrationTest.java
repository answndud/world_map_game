package com.worldmap.game.populationbattle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.worldmap.game.populationbattle.domain.PopulationBattleGameAttemptRepository;
import com.worldmap.game.populationbattle.domain.PopulationBattleGameSessionRepository;
import com.worldmap.game.populationbattle.domain.PopulationBattleGameStage;
import com.worldmap.game.populationbattle.domain.PopulationBattleGameStageRepository;
import java.util.UUID;
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
class PopulationBattleGameFlowIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private PopulationBattleGameSessionRepository populationBattleGameSessionRepository;

	@Autowired
	private PopulationBattleGameStageRepository populationBattleGameStageRepository;

	@Autowired
	private PopulationBattleGameAttemptRepository populationBattleGameAttemptRepository;

	@Test
	void populationBattleContinuesBeyondFiveStages() throws Exception {
		MockHttpSession browserSession = new MockHttpSession();
		UUID sessionId = UUID.fromString(startGame("battle-player", browserSession));

		for (int stageNumber = 1; stageNumber <= 7; stageNumber++) {
			mockMvc.perform(get("/api/games/population-battle/sessions/{sessionId}/state", sessionId).session(browserSession))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.stageNumber").value(stageNumber))
				.andExpect(jsonPath("$.options.length()").value(2))
				.andExpect(jsonPath("$.livesRemaining").value(3))
				.andExpect(jsonPath("$.questionPrompt").value("두 나라 중 인구가 더 많은 나라를 고르세요."))
				.andExpect(jsonPath("$.difficultyLabel").isNotEmpty());

			PopulationBattleGameStage stage = populationBattleGameStageRepository.findBySessionIdAndStageNumber(sessionId, stageNumber)
				.orElseThrow();

			MvcResult answerResult = mockMvc.perform(
				post("/api/games/population-battle/sessions/{sessionId}/answer", sessionId)
					.session(browserSession)
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

		mockMvc.perform(get("/api/games/population-battle/sessions/{sessionId}", sessionId).session(browserSession))
			.andExpect(status().isNotFound());
	}

	@Test
	void wrongAnswerConsumesLifeAndKeepsSameStage() throws Exception {
		MockHttpSession browserSession = new MockHttpSession();
		UUID sessionId = UUID.fromString(startGame("battle-life", browserSession));
		PopulationBattleGameStage firstStage = populationBattleGameStageRepository.findBySessionIdAndStageNumber(sessionId, 1)
			.orElseThrow();
		int wrongOptionNumber = findWrongOptionNumber(firstStage.getCorrectOptionNumber());

		mockMvc.perform(
			post("/api/games/population-battle/sessions/{sessionId}/answer", sessionId)
				.session(browserSession)
				.contentType("application/json")
				.content(answerPayload(1, wrongOptionNumber))
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.outcome").value("WRONG"))
			.andExpect(jsonPath("$.gameStatus").value("IN_PROGRESS"))
			.andExpect(jsonPath("$.livesRemaining").value(2))
			.andExpect(jsonPath("$.nextStageNumber").value(1))
			.andExpect(jsonPath("$.clearedStageCount").value(0));

		mockMvc.perform(get("/api/games/population-battle/sessions/{sessionId}/state", sessionId).session(browserSession))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.stageNumber").value(1))
			.andExpect(jsonPath("$.livesRemaining").value(2));

		assertThat(populationBattleGameAttemptRepository.findAllByStageSessionIdOrderByStageStageNumberAscAttemptNumberAsc(sessionId))
			.hasSize(1);
	}

	@Test
	void threeWrongAnswersLeadToGameOver() throws Exception {
		MockHttpSession browserSession = new MockHttpSession();
		UUID sessionId = UUID.fromString(startGame("battle-game-over", browserSession));
		PopulationBattleGameStage firstStage = populationBattleGameStageRepository.findBySessionIdAndStageNumber(sessionId, 1)
			.orElseThrow();
		int wrongOptionNumber = findWrongOptionNumber(firstStage.getCorrectOptionNumber());

		for (int attempt = 1; attempt <= 2; attempt++) {
			mockMvc.perform(
				post("/api/games/population-battle/sessions/{sessionId}/answer", sessionId)
					.session(browserSession)
					.contentType("application/json")
					.content(answerPayload(1, wrongOptionNumber))
			)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.outcome").value("WRONG"));
		}

		mockMvc.perform(
			post("/api/games/population-battle/sessions/{sessionId}/answer", sessionId)
				.session(browserSession)
				.contentType("application/json")
				.content(answerPayload(1, wrongOptionNumber))
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.outcome").value("GAME_OVER"))
			.andExpect(jsonPath("$.gameStatus").value("GAME_OVER"))
			.andExpect(jsonPath("$.livesRemaining").value(0))
			.andExpect(jsonPath("$.nextStageNumber").doesNotExist());

		mockMvc.perform(get("/api/games/population-battle/sessions/{sessionId}", sessionId).session(browserSession))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("GAME_OVER"))
			.andExpect(jsonPath("$.livesRemaining").value(0))
			.andExpect(jsonPath("$.stages[0].status").value("FAILED"))
			.andExpect(jsonPath("$.stages[0].attempts.length()").value(3));
	}

	@Test
	void restartReusesSameSessionAndResetsProgress() throws Exception {
		MockHttpSession browserSession = new MockHttpSession();
		UUID sessionId = UUID.fromString(startGame("battle-restart", browserSession));
		PopulationBattleGameStage firstStage = populationBattleGameStageRepository.findBySessionIdAndStageNumber(sessionId, 1)
			.orElseThrow();
		int wrongOptionNumber = findWrongOptionNumber(firstStage.getCorrectOptionNumber());

		for (int attempt = 1; attempt <= 3; attempt++) {
			mockMvc.perform(
				post("/api/games/population-battle/sessions/{sessionId}/answer", sessionId)
					.session(browserSession)
					.contentType("application/json")
					.content(answerPayload(1, wrongOptionNumber))
			)
				.andExpect(status().isOk());
		}

		mockMvc.perform(post("/api/games/population-battle/sessions/{sessionId}/restart", sessionId).session(browserSession))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.sessionId").value(sessionId.toString()))
			.andExpect(jsonPath("$.status").value("IN_PROGRESS"))
			.andExpect(jsonPath("$.livesRemaining").value(3))
			.andExpect(jsonPath("$.totalStages").value(1))
			.andExpect(jsonPath("$.playPageUrl").value("/games/population-battle/play/" + sessionId));

		mockMvc.perform(get("/api/games/population-battle/sessions/{sessionId}/state", sessionId).session(browserSession))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.stageNumber").value(1))
			.andExpect(jsonPath("$.livesRemaining").value(3))
			.andExpect(jsonPath("$.clearedStageCount").value(0))
			.andExpect(jsonPath("$.totalScore").value(0));

		assertThat(populationBattleGameAttemptRepository.findAllByStageSessionIdOrderByStageStageNumberAscAttemptNumberAsc(sessionId))
			.isEmpty();
		assertThat(populationBattleGameStageRepository.findAllBySessionIdOrderByStageNumber(sessionId))
			.hasSize(1);
	}

	@Test
	void resultPageHidesSelectionAndAnswerDetailsForClearedStage() throws Exception {
		MockHttpSession browserSession = new MockHttpSession();
		UUID sessionId = UUID.fromString(startGame("battle-result-hide", browserSession));
		PopulationBattleGameStage firstStage = populationBattleGameStageRepository.findBySessionIdAndStageNumber(sessionId, 1)
			.orElseThrow();
		int wrongOptionNumber = findWrongOptionNumber(firstStage.getCorrectOptionNumber());
		String wrongCountryName = wrongOptionNumber == 1 ? firstStage.getOptionOneCountryName() : firstStage.getOptionTwoCountryName();
		String correctCountryName = firstStage.getCorrectCountryName();

		mockMvc.perform(
			post("/api/games/population-battle/sessions/{sessionId}/answer", sessionId)
				.session(browserSession)
				.contentType("application/json")
				.content(answerPayload(1, wrongOptionNumber))
		)
			.andExpect(status().isOk());

		mockMvc.perform(
			post("/api/games/population-battle/sessions/{sessionId}/answer", sessionId)
				.session(browserSession)
				.contentType("application/json")
				.content(answerPayload(1, firstStage.getCorrectOptionNumber()))
		)
			.andExpect(status().isOk());

		PopulationBattleGameStage secondStage = populationBattleGameStageRepository.findBySessionIdAndStageNumber(sessionId, 2)
			.orElseThrow();
		int secondStageWrongOptionNumber = findWrongOptionNumber(secondStage.getCorrectOptionNumber());
		for (int attempt = 1; attempt <= 3; attempt++) {
			mockMvc.perform(
				post("/api/games/population-battle/sessions/{sessionId}/answer", sessionId)
					.session(browserSession)
					.contentType("application/json")
					.content(answerPayload(2, secondStageWrongOptionNumber))
			)
				.andExpect(status().isOk());
		}

		mockMvc.perform(get("/games/population-battle/result/{sessionId}", sessionId).session(browserSession))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("1차 오답 / 하트 2")))
			.andExpect(content().string(containsString("2차 정답 / 점수 +")))
			.andExpect(content().string(not(containsString("1차: " + wrongCountryName))))
			.andExpect(content().string(not(containsString("2차: " + correctCountryName))))
			.andExpect(content().string(not(containsString(String.valueOf(firstStage.getOptionOnePopulation())))))
			.andExpect(content().string(not(containsString(String.valueOf(firstStage.getOptionTwoPopulation())))))
			.andExpect(content().string(not(containsString("<th>정답</th>"))));
	}

	private String startGame(String nickname, MockHttpSession browserSession) throws Exception {
		MvcResult result = mockMvc.perform(
			post("/api/games/population-battle/sessions")
				.session(browserSession)
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
}
