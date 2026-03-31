package com.worldmap.ranking;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.worldmap.ranking.domain.LeaderboardGameMode;
import com.worldmap.ranking.domain.LeaderboardRecord;
import com.worldmap.ranking.domain.LeaderboardRecordRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
	"worldmap.legacy.rollback.enabled=false",
	"spring.data.redis.host=127.0.0.1",
	"spring.data.redis.port=6390"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RedisUnavailableLeaderboardFallbackIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private LeaderboardRecordRepository leaderboardRecordRepository;

	@BeforeEach
	void clearRecords() {
		leaderboardRecordRepository.deleteAll();
	}

	@Test
	void rankingsApiFallsBackToDatabaseWhenRedisIsUnavailable() throws Exception {
		saveLocationRecord("redisless-player");

		mockMvc.perform(get("/api/rankings/location").param("scope", "ALL").param("limit", "5"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.gameMode").value("LOCATION"))
			.andExpect(jsonPath("$.entries[0].playerNickname").value("redisless-player"))
			.andExpect(jsonPath("$.entries[0].totalScore").value(420));
	}

	@Test
	void rankingPageRendersInitialBoardFromDatabaseWhenRedisIsUnavailable() throws Exception {
		saveLocationRecord("ranking-fallback");

		mockMvc.perform(get("/ranking"))
			.andExpect(status().isOk())
			.andExpect(view().name("ranking/index"))
			.andExpect(content().string(containsString("ranking-location-all-body")))
			.andExpect(content().string(containsString("ranking-fallback")));
	}

	@Test
	void statsPageRendersDailyTopFromDatabaseWhenRedisIsUnavailable() throws Exception {
		saveLocationRecord("stats-fallback");

		mockMvc.perform(get("/stats"))
			.andExpect(status().isOk())
			.andExpect(view().name("stats/index"))
			.andExpect(content().string(containsString("서비스 현황")))
			.andExpect(content().string(containsString("stats-fallback")));
	}

	private void saveLocationRecord(String nickname) {
		leaderboardRecordRepository.saveAndFlush(
			LeaderboardRecord.create(
				"fallback:%s".formatted(nickname),
				UUID.randomUUID(),
				LeaderboardGameMode.LOCATION,
				nickname,
				null,
				"guest-%s".formatted(nickname),
				420,
				4_200L,
				7,
				9,
				LocalDateTime.now()
			)
		);
	}
}
