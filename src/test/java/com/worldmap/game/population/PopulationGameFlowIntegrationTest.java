package com.worldmap.game.population;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.worldmap.game.population.domain.PopulationGameRound;
import com.worldmap.game.population.domain.PopulationGameRoundRepository;
import com.worldmap.game.population.domain.PopulationGameSessionRepository;
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
	private PopulationGameRoundRepository populationGameRoundRepository;

	@Test
	void populationGameCanBePlayedToCompletion() throws Exception {
		UUID sessionId = UUID.fromString(startGame("population-player"));
		int totalRounds = populationGameSessionRepository.findById(sessionId).orElseThrow().getTotalRounds();

		for (int roundNumber = 1; roundNumber <= totalRounds; roundNumber++) {
			mockMvc.perform(get("/api/games/population/sessions/{sessionId}/round", sessionId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.roundNumber").value(roundNumber))
				.andExpect(jsonPath("$.options", hasSize(4)));

			PopulationGameRound round = populationGameRoundRepository.findBySessionIdAndRoundNumber(sessionId, roundNumber)
				.orElseThrow();

			MvcResult answerResult = mockMvc.perform(
				post("/api/games/population/sessions/{sessionId}/answer", sessionId)
					.contentType("application/json")
					.content(answerPayload(roundNumber, round.getCorrectOptionNumber()))
			)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.roundNumber").value(roundNumber))
				.andExpect(jsonPath("$.awardedScore").value(100))
				.andReturn();

			JsonNode answerJson = objectMapper.readTree(answerResult.getResponse().getContentAsString());

			if (roundNumber < totalRounds) {
				assertThat(answerJson.get("gameStatus").asText()).isEqualTo("IN_PROGRESS");
			} else {
				assertThat(answerJson.get("gameStatus").asText()).isEqualTo("FINISHED");
			}
		}

		mockMvc.perform(get("/api/games/population/sessions/{sessionId}", sessionId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("FINISHED"))
			.andExpect(jsonPath("$.answeredRoundCount").value(totalRounds))
			.andExpect(jsonPath("$.totalScore").value(totalRounds * 100))
			.andExpect(jsonPath("$.rounds", hasSize(totalRounds)));
	}

	@Test
	void sameRoundCannotBeSubmittedTwice() throws Exception {
		UUID sessionId = UUID.fromString(startGame("population-duplicate"));
		PopulationGameRound firstRound = populationGameRoundRepository.findBySessionIdAndRoundNumber(sessionId, 1)
			.orElseThrow();

		mockMvc.perform(
			post("/api/games/population/sessions/{sessionId}/answer", sessionId)
				.contentType("application/json")
				.content(answerPayload(1, firstRound.getCorrectOptionNumber()))
		).andExpect(status().isOk());

		mockMvc.perform(
			post("/api/games/population/sessions/{sessionId}/answer", sessionId)
				.contentType("application/json")
				.content(answerPayload(1, firstRound.getCorrectOptionNumber()))
		)
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.message").value("현재 진행 중인 라운드와 일치하지 않습니다."));
	}

	private String startGame(String nickname) throws Exception {
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

	private String answerPayload(Integer roundNumber, Integer selectedOptionNumber) {
		return """
			{
			  "roundNumber": %d,
			  "selectedOptionNumber": %d
			}
			""".formatted(roundNumber, selectedOptionNumber);
	}
}
