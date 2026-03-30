package com.worldmap.game.flag;

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
import com.worldmap.country.domain.Continent;
import com.worldmap.game.flag.application.FlagQuestionCountryPoolService;
import com.worldmap.game.flag.domain.FlagGameAttemptRepository;
import com.worldmap.game.flag.domain.FlagGameStage;
import com.worldmap.game.flag.domain.FlagGameStageRepository;
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
class FlagGameFlowIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private FlagGameStageRepository flagGameStageRepository;

	@Autowired
	private FlagGameAttemptRepository flagGameAttemptRepository;

	@Autowired
	private FlagQuestionCountryPoolService flagQuestionCountryPoolService;

	@Test
	void flagGameContinuesBeyondFiveStages() throws Exception {
		MockHttpSession browserSession = new MockHttpSession();
		UUID sessionId = UUID.fromString(startGame("flag-player", browserSession));

		for (int stageNumber = 1; stageNumber <= 7; stageNumber++) {
			MvcResult stateResult = mockMvc.perform(get("/api/games/flag/sessions/{sessionId}/state", sessionId).session(browserSession))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.stageNumber").value(stageNumber))
				.andExpect(jsonPath("$.options.length()").value(4))
				.andExpect(jsonPath("$.livesRemaining").value(3))
				.andExpect(jsonPath("$.targetFlagRelativePath").value(org.hamcrest.Matchers.startsWith("/images/flags/")))
				.andExpect(jsonPath("$.difficultyLabel").isNotEmpty())
				.andExpect(jsonPath("$.difficultyGuide").isNotEmpty())
				.andReturn();

			assertOptionsUseKoreanCountryNames(stateResult);

			FlagGameStage stage = flagGameStageRepository.findBySessionIdAndStageNumber(sessionId, stageNumber)
				.orElseThrow();

			MvcResult answerResult = mockMvc.perform(
				post("/api/games/flag/sessions/{sessionId}/answer", sessionId)
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
			assertThat(answerJson.get("targetFlagRelativePath").asText()).startsWith("/images/flags/");
			assertThat(answerJson.get("nextDifficultyGuide").asText()).isNotBlank();
		}

		mockMvc.perform(get("/api/games/flag/sessions/{sessionId}", sessionId).session(browserSession))
			.andExpect(status().isNotFound());
	}

	@Test
	void earlyRoundTargetsStayWithinContinentsThatHaveEnoughSameContinentDistractors() throws Exception {
		MockHttpSession browserSession = new MockHttpSession();
		UUID sessionId = UUID.fromString(startGame("flag-continent", browserSession));
		FlagGameStage firstStage = flagGameStageRepository.findBySessionIdAndStageNumber(sessionId, 1)
			.orElseThrow();

		Continent continent = flagQuestionCountryPoolService.findAvailableCountry(firstStage.getCountryIso3Code())
			.orElseThrow()
			.continent();

		assertThat(continent).isNotIn(Continent.NORTH_AMERICA, Continent.OCEANIA);
	}

	@Test
	void flagGameReturnsKoreanCountryNamesInAnswerPayload() throws Exception {
		MockHttpSession browserSession = new MockHttpSession();
		UUID sessionId = UUID.fromString(startGame("flag-korean", browserSession));
		FlagGameStage firstStage = flagGameStageRepository.findBySessionIdAndStageNumber(sessionId, 1)
			.orElseThrow();

		MvcResult answerResult = mockMvc.perform(
			post("/api/games/flag/sessions/{sessionId}/answer", sessionId)
				.session(browserSession)
				.contentType("application/json")
				.content(answerPayload(1, firstStage.getCorrectOptionNumber()))
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.correct").value(true))
			.andReturn();

		JsonNode answerJson = objectMapper.readTree(answerResult.getResponse().getContentAsString());
		assertThat(answerJson.get("selectedCountryName").asText()).matches(".*[가-힣].*");
		assertThat(answerJson.get("correctCountryName").asText()).matches(".*[가-힣].*");
	}

	@Test
	void playPageRendersAccessibleGameOverDialogShell() throws Exception {
		MockHttpSession browserSession = new MockHttpSession();
		String sessionId = startGame("flag-dialog", browserSession);

		mockMvc.perform(get("/games/flag/play/{sessionId}", sessionId).session(browserSession))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("role=\"dialog\"")))
			.andExpect(content().string(containsString("aria-describedby=\"flag-game-over-summary\"")))
			.andExpect(content().string(containsString("tabindex=\"-1\"")));
	}

	@Test
	void duplicateCorrectAnswerIsRejectedAfterStageAdvances() throws Exception {
		MockHttpSession browserSession = new MockHttpSession();
		UUID sessionId = UUID.fromString(startGame("flag-duplicate", browserSession));
		FlagGameStage firstStage = flagGameStageRepository.findBySessionIdAndStageNumber(sessionId, 1)
			.orElseThrow();

		mockMvc.perform(
			post("/api/games/flag/sessions/{sessionId}/answer", sessionId)
				.session(browserSession)
				.contentType("application/json")
				.content(answerPayload(1, firstStage.getCorrectOptionNumber()))
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.correct").value(true))
			.andExpect(jsonPath("$.nextStageNumber").value(2));

		mockMvc.perform(
			post("/api/games/flag/sessions/{sessionId}/answer", sessionId)
				.session(browserSession)
				.contentType("application/json")
				.content(answerPayload(1, firstStage.getCorrectOptionNumber()))
		)
			.andExpect(status().isConflict());

		assertThat(flagGameAttemptRepository.findAllByStageSessionIdOrderByStageStageNumberAscAttemptNumberAsc(sessionId))
			.hasSize(1);
		assertThat(flagGameStageRepository.findAllBySessionIdOrderByStageNumber(sessionId))
			.hasSize(2);
	}

	@Test
	void wrongAnswerConsumesLifeAndKeepsSameStage() throws Exception {
		MockHttpSession browserSession = new MockHttpSession();
		UUID sessionId = UUID.fromString(startGame("flag-life", browserSession));
		FlagGameStage firstStage = flagGameStageRepository.findBySessionIdAndStageNumber(sessionId, 1)
			.orElseThrow();
		int wrongOptionNumber = findWrongOptionNumber(firstStage.getCorrectOptionNumber());

		mockMvc.perform(
			post("/api/games/flag/sessions/{sessionId}/answer", sessionId)
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

		mockMvc.perform(get("/api/games/flag/sessions/{sessionId}/state", sessionId).session(browserSession))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.stageNumber").value(1))
			.andExpect(jsonPath("$.livesRemaining").value(2));

		assertThat(flagGameAttemptRepository.findAllByStageSessionIdOrderByStageStageNumberAscAttemptNumberAsc(sessionId))
			.hasSize(1);
	}

	@Test
	void staleDuplicateWrongAnswerIsRejectedWithoutConsumingExtraLife() throws Exception {
		MockHttpSession browserSession = new MockHttpSession();
		UUID sessionId = UUID.fromString(startGame("stale-flag", browserSession));
		FlagGameStage firstStage = flagGameStageRepository.findBySessionIdAndStageNumber(sessionId, 1)
			.orElseThrow();
		int wrongOptionNumber = firstStage.getCorrectOptionNumber() == 1 ? 2 : 1;
		String stalePayload = answerPayload(1, firstStage.getId(), firstStage.nextAttemptNumber(), wrongOptionNumber);

		mockMvc.perform(
			post("/api/games/flag/sessions/{sessionId}/answer", sessionId)
				.session(browserSession)
				.contentType("application/json")
				.content(stalePayload)
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.outcome").value("WRONG"))
			.andExpect(jsonPath("$.livesRemaining").value(2));

		mockMvc.perform(
			post("/api/games/flag/sessions/{sessionId}/answer", sessionId)
				.session(browserSession)
				.contentType("application/json")
				.content(stalePayload)
		)
			.andExpect(status().isConflict());

		mockMvc.perform(get("/api/games/flag/sessions/{sessionId}/state", sessionId).session(browserSession))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.livesRemaining").value(2));

		assertThat(flagGameAttemptRepository.findAllByStageSessionIdOrderByStageStageNumberAscAttemptNumberAsc(sessionId))
			.hasSize(1);
	}

	@Test
	void threeWrongAnswersLeadToGameOver() throws Exception {
		MockHttpSession browserSession = new MockHttpSession();
		UUID sessionId = UUID.fromString(startGame("flag-game-over", browserSession));
		FlagGameStage firstStage = flagGameStageRepository.findBySessionIdAndStageNumber(sessionId, 1)
			.orElseThrow();
		int wrongOptionNumber = findWrongOptionNumber(firstStage.getCorrectOptionNumber());

		for (int attempt = 1; attempt <= 2; attempt++) {
			mockMvc.perform(
				post("/api/games/flag/sessions/{sessionId}/answer", sessionId)
					.session(browserSession)
					.contentType("application/json")
					.content(answerPayload(1, wrongOptionNumber))
			)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.outcome").value("WRONG"));
		}

		mockMvc.perform(
			post("/api/games/flag/sessions/{sessionId}/answer", sessionId)
				.session(browserSession)
				.contentType("application/json")
				.content(answerPayload(1, wrongOptionNumber))
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.outcome").value("GAME_OVER"))
			.andExpect(jsonPath("$.gameStatus").value("GAME_OVER"))
			.andExpect(jsonPath("$.livesRemaining").value(0))
			.andExpect(jsonPath("$.nextStageNumber").doesNotExist());

		mockMvc.perform(get("/api/games/flag/sessions/{sessionId}", sessionId).session(browserSession))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("GAME_OVER"))
			.andExpect(jsonPath("$.livesRemaining").value(0))
			.andExpect(jsonPath("$.stages[0].status").value("FAILED"))
			.andExpect(jsonPath("$.stages[0].attempts.length()").value(3));
	}

	@Test
	void restartReusesSameSessionAndResetsProgress() throws Exception {
		MockHttpSession browserSession = new MockHttpSession();
		UUID sessionId = UUID.fromString(startGame("flag-restart", browserSession));
		FlagGameStage firstStage = flagGameStageRepository.findBySessionIdAndStageNumber(sessionId, 1)
			.orElseThrow();
		int wrongOptionNumber = findWrongOptionNumber(firstStage.getCorrectOptionNumber());

		for (int attempt = 1; attempt <= 3; attempt++) {
			mockMvc.perform(
				post("/api/games/flag/sessions/{sessionId}/answer", sessionId)
					.session(browserSession)
					.contentType("application/json")
					.content(answerPayload(1, wrongOptionNumber))
			)
				.andExpect(status().isOk());
		}

		mockMvc.perform(post("/api/games/flag/sessions/{sessionId}/restart", sessionId).session(browserSession))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.sessionId").value(sessionId.toString()))
			.andExpect(jsonPath("$.status").value("IN_PROGRESS"))
			.andExpect(jsonPath("$.livesRemaining").value(3))
			.andExpect(jsonPath("$.totalStages").value(1))
			.andExpect(jsonPath("$.playPageUrl").value("/games/flag/play/" + sessionId));

		mockMvc.perform(get("/api/games/flag/sessions/{sessionId}/state", sessionId).session(browserSession))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.stageNumber").value(1))
			.andExpect(jsonPath("$.livesRemaining").value(3))
			.andExpect(jsonPath("$.clearedStageCount").value(0))
			.andExpect(jsonPath("$.totalScore").value(0));

		assertThat(flagGameAttemptRepository.findAllByStageSessionIdOrderByStageStageNumberAscAttemptNumberAsc(sessionId))
			.isEmpty();
		assertThat(flagGameStageRepository.findAllBySessionIdOrderByStageNumber(sessionId))
			.hasSize(1);
	}

	@Test
	void resultPageHidesSelectionAndAnswerDetailsForClearedStage() throws Exception {
		MockHttpSession browserSession = new MockHttpSession();
		UUID sessionId = UUID.fromString(startGame("flag-result-hide", browserSession));
		FlagGameStage firstStage = flagGameStageRepository.findBySessionIdAndStageNumber(sessionId, 1)
			.orElseThrow();
		int wrongOptionNumber = findWrongOptionNumber(firstStage.getCorrectOptionNumber());
		String wrongCountryName = firstStage.getOptions().get(wrongOptionNumber - 1);
		String correctCountryName = firstStage.getOptions().get(firstStage.getCorrectOptionNumber() - 1);

		mockMvc.perform(
			post("/api/games/flag/sessions/{sessionId}/answer", sessionId)
				.session(browserSession)
				.contentType("application/json")
				.content(answerPayload(1, wrongOptionNumber))
		)
			.andExpect(status().isOk());

		mockMvc.perform(
			post("/api/games/flag/sessions/{sessionId}/answer", sessionId)
				.session(browserSession)
				.contentType("application/json")
				.content(answerPayload(1, firstStage.getCorrectOptionNumber()))
		)
			.andExpect(status().isOk());

		FlagGameStage secondStage = flagGameStageRepository.findBySessionIdAndStageNumber(sessionId, 2)
			.orElseThrow();
		int secondStageWrongOptionNumber = findWrongOptionNumber(secondStage.getCorrectOptionNumber());
		for (int attempt = 1; attempt <= 3; attempt++) {
			mockMvc.perform(
				post("/api/games/flag/sessions/{sessionId}/answer", sessionId)
					.session(browserSession)
					.contentType("application/json")
					.content(answerPayload(2, secondStageWrongOptionNumber))
			)
				.andExpect(status().isOk());
		}

		mockMvc.perform(get("/games/flag/result/{sessionId}", sessionId).session(browserSession))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("구간")))
			.andExpect(content().string(containsString("기본 라운드")))
			.andExpect(content().string(containsString("1차 오답 / 하트 2")))
			.andExpect(content().string(containsString("2차 정답 / 점수 +")))
			.andExpect(content().string(not(containsString(wrongCountryName))))
			.andExpect(content().string(not(containsString(correctCountryName))))
			.andExpect(content().string(not(containsString("정답 국가"))));
	}

	private String startGame(String nickname, MockHttpSession browserSession) throws Exception {
		MvcResult result = mockMvc.perform(
			post("/api/games/flag/sessions")
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

	private String answerPayload(
		Integer stageNumber,
		Long stageId,
		Integer expectedAttemptNumber,
		Integer selectedOptionNumber
	) {
		return """
			{
			  "stageNumber": %d,
			  "stageId": %d,
			  "expectedAttemptNumber": %d,
			  "selectedOptionNumber": %d
			}
			""".formatted(stageNumber, stageId, expectedAttemptNumber, selectedOptionNumber);
	}

	private int findWrongOptionNumber(int correctOptionNumber) {
		return correctOptionNumber == 1 ? 2 : 1;
	}

	private void assertOptionsUseKoreanCountryNames(MvcResult stateResult) throws Exception {
		JsonNode stateJson = objectMapper.readTree(stateResult.getResponse().getContentAsString());
		for (JsonNode option : stateJson.get("options")) {
			assertThat(option.get("countryName").asText()).matches(".*[가-힣].*");
		}
	}
}
