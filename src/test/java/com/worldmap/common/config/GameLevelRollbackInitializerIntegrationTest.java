package com.worldmap.common.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.worldmap.country.domain.Country;
import com.worldmap.country.domain.CountryRepository;
import com.worldmap.game.location.domain.LocationGameAttempt;
import com.worldmap.game.location.domain.LocationGameAttemptRepository;
import com.worldmap.game.location.domain.LocationGameLevel;
import com.worldmap.game.location.domain.LocationGameSession;
import com.worldmap.game.location.domain.LocationGameSessionRepository;
import com.worldmap.game.location.domain.LocationGameStage;
import com.worldmap.game.location.domain.LocationGameStageRepository;
import com.worldmap.game.population.domain.PopulationGameAttempt;
import com.worldmap.game.population.domain.PopulationGameAttemptRepository;
import com.worldmap.game.population.domain.PopulationGameLevel;
import com.worldmap.game.population.domain.PopulationGameSession;
import com.worldmap.game.population.domain.PopulationGameSessionRepository;
import com.worldmap.game.population.domain.PopulationGameStage;
import com.worldmap.game.population.domain.PopulationGameStageRepository;
import com.worldmap.ranking.domain.LeaderboardGameLevel;
import com.worldmap.ranking.domain.LeaderboardGameMode;
import com.worldmap.ranking.domain.LeaderboardRecord;
import com.worldmap.ranking.domain.LeaderboardRecordRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class GameLevelRollbackInitializerIntegrationTest {

	@Autowired
	private GameLevelRollbackInitializer initializer;

	@Autowired
	private CountryRepository countryRepository;

	@Autowired
	private LocationGameSessionRepository locationGameSessionRepository;

	@Autowired
	private LocationGameStageRepository locationGameStageRepository;

	@Autowired
	private LocationGameAttemptRepository locationGameAttemptRepository;

	@Autowired
	private PopulationGameSessionRepository populationGameSessionRepository;

	@Autowired
	private PopulationGameStageRepository populationGameStageRepository;

	@Autowired
	private PopulationGameAttemptRepository populationGameAttemptRepository;

	@Autowired
	private LeaderboardRecordRepository leaderboardRecordRepository;

	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void initializerPurgesLevelTwoRowsAndKeepsLevelOneRows() {
		Country korea = countryRepository.findByIso3CodeIgnoreCase("KOR").orElseThrow();
		Country japan = countryRepository.findByIso3CodeIgnoreCase("JPN").orElseThrow();
		LocalDateTime now = LocalDateTime.now();
		long levelOneLocationSessionsBefore = count("select count(*) from location_game_session where game_level = 'LEVEL_1'");
		long levelOnePopulationSessionsBefore = count("select count(*) from population_game_session where game_level = 'LEVEL_1'");
		long levelOneLeaderboardRecordsBefore = count("select count(*) from leaderboard_record where game_level = 'LEVEL_1'");

		createLocationRun(korea, LocationGameLevel.LEVEL_1, now.minusMinutes(5), "legacy-location-l1");
		createLocationRun(japan, LocationGameLevel.LEVEL_2, now.minusMinutes(4), "legacy-location-l2");
		createPopulationRun(korea, PopulationGameLevel.LEVEL_1, now.minusMinutes(3), "legacy-population-l1");
		createPopulationRun(japan, PopulationGameLevel.LEVEL_2, now.minusMinutes(2), "legacy-population-l2");

		stringRedisTemplate.opsForZSet().add("test:leaderboard:all:location:l2", "1000", 1000.0);
		stringRedisTemplate.opsForZSet().add(
			"test:leaderboard:daily:population:l2:" + LocalDate.now(),
			"1001",
			1001.0
		);

		initializer.run(new DefaultApplicationArguments(new String[0]));

		assertThat(count("select count(*) from location_game_session where game_level = 'LEVEL_2'")).isZero();
		assertThat(count("select count(*) from population_game_session where game_level = 'LEVEL_2'")).isZero();
		assertThat(count("select count(*) from leaderboard_record where game_level = 'LEVEL_2'")).isZero();

		assertThat(count("select count(*) from location_game_session where game_level = 'LEVEL_1'"))
			.isEqualTo(levelOneLocationSessionsBefore + 1);
		assertThat(count("select count(*) from population_game_session where game_level = 'LEVEL_1'"))
			.isEqualTo(levelOnePopulationSessionsBefore + 1);
		assertThat(count("select count(*) from leaderboard_record where game_level = 'LEVEL_1'"))
			.isEqualTo(levelOneLeaderboardRecordsBefore + 2);

		assertThat(count("""
			select count(*)
			from location_game_attempt attempt
			join location_game_stage stage on attempt.stage_id = stage.id
			join location_game_session session on stage.session_id = session.id
			where session.game_level = 'LEVEL_2'
			""")).isZero();
		assertThat(count("""
			select count(*)
			from population_game_attempt attempt
			join population_game_stage stage on attempt.stage_id = stage.id
			join population_game_session session on stage.session_id = session.id
			where session.game_level = 'LEVEL_2'
			""")).isZero();

		assertThat(stringRedisTemplate.keys("test:leaderboard:*:*:l2*")).isEmpty();
	}

	private void createLocationRun(Country country, LocationGameLevel gameLevel, LocalDateTime finishedAt, String signature) {
		LocationGameSession session = locationGameSessionRepository.save(LocationGameSession.ready(signature, gameLevel, 1));
		LocationGameStage stage = locationGameStageRepository.save(LocationGameStage.create(session, 1, country));
		locationGameAttemptRepository.save(LocationGameAttempt.create(stage, 1, country, true, 3, finishedAt.minusSeconds(1)));
		leaderboardRecordRepository.save(
			LeaderboardRecord.create(
				signature,
				session.getId(),
				LeaderboardGameMode.LOCATION,
				gameLevel == LocationGameLevel.LEVEL_2 ? LeaderboardGameLevel.LEVEL_2 : LeaderboardGameLevel.LEVEL_1,
				signature,
				null,
				signature,
				100,
				100L,
				1,
				1,
				finishedAt
			)
		);
	}

	private void createPopulationRun(Country country, PopulationGameLevel gameLevel, LocalDateTime finishedAt, String signature) {
		PopulationGameSession session = populationGameSessionRepository.save(PopulationGameSession.ready(signature, gameLevel, 1));
		List<Long> options = List.of(
			country.getPopulation(),
			country.getPopulation() + 1_000_000L,
			country.getPopulation() + 2_000_000L,
			country.getPopulation() + 3_000_000L
		);
		PopulationGameStage stage = populationGameStageRepository.save(
			PopulationGameStage.create(session, 1, country, options, 1)
		);
		populationGameAttemptRepository.save(
			PopulationGameAttempt.create(stage, 1, 1, country.getPopulation(), true, 3, finishedAt.minusSeconds(1))
		);
		leaderboardRecordRepository.save(
			LeaderboardRecord.create(
				signature,
				session.getId(),
				LeaderboardGameMode.POPULATION,
				gameLevel == PopulationGameLevel.LEVEL_2 ? LeaderboardGameLevel.LEVEL_2 : LeaderboardGameLevel.LEVEL_1,
				signature,
				null,
				signature,
				100,
				100L,
				1,
				1,
				finishedAt
			)
		);
	}

	private long count(String sql) {
		Long value = jdbcTemplate.queryForObject(sql, Long.class);
		return value == null ? 0L : value;
	}
}
