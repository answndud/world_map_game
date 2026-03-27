package com.worldmap.common.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.worldmap.country.domain.Country;
import com.worldmap.country.domain.CountryRepository;
import com.worldmap.game.location.domain.LocationGameAttempt;
import com.worldmap.game.location.domain.LocationGameAttemptRepository;
import com.worldmap.game.location.domain.LocationGameSession;
import com.worldmap.game.location.domain.LocationGameSessionRepository;
import com.worldmap.game.location.domain.LocationGameStage;
import com.worldmap.game.location.domain.LocationGameStageRepository;
import com.worldmap.game.population.domain.PopulationGameAttempt;
import com.worldmap.game.population.domain.PopulationGameAttemptRepository;
import com.worldmap.game.population.domain.PopulationGameSession;
import com.worldmap.game.population.domain.PopulationGameSessionRepository;
import com.worldmap.game.population.domain.PopulationGameStage;
import com.worldmap.game.population.domain.PopulationGameStageRepository;
import com.worldmap.ranking.domain.LeaderboardGameMode;
import com.worldmap.ranking.domain.LeaderboardRecord;
import com.worldmap.ranking.domain.LeaderboardRecordRepository;
import java.time.LocalDateTime;
import java.util.List;
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
		ensureLegacyGameLevelColumns();
		jdbcTemplate.execute("alter table leaderboard_record alter column game_level set not null");
		replaceLeaderboardGameModeCheckWithLegacySubset();
		Country korea = countryRepository.findByIso3CodeIgnoreCase("KOR").orElseThrow();
		Country japan = countryRepository.findByIso3CodeIgnoreCase("JPN").orElseThrow();
		LocalDateTime now = LocalDateTime.now();
		long locationSessionsBefore = count("select count(*) from location_game_session");
		long populationSessionsBefore = count("select count(*) from population_game_session");
		long leaderboardRecordsBefore = count("select count(*) from leaderboard_record");

		createLocationRun(korea, false, now.minusMinutes(5), "legacy-location-l1");
		createLocationRun(japan, true, now.minusMinutes(4), "legacy-location-l2");
		createPopulationRun(korea, false, now.minusMinutes(3), "legacy-population-l1");
		createPopulationRun(japan, true, now.minusMinutes(2), "legacy-population-l2");

		stringRedisTemplate.opsForZSet().add("test:leaderboard:all:location:l2", "1000", 1000.0);
		stringRedisTemplate.opsForZSet().add("test:leaderboard:daily:population:l2:2099-01-01", "1001", 1001.0);

		initializer.run(new DefaultApplicationArguments(new String[0]));

		assertThat(count("select count(*) from location_game_session where game_level = 'LEVEL_2'")).isZero();
		assertThat(count("select count(*) from population_game_session where game_level = 'LEVEL_2'")).isZero();
		assertThat(count("select count(*) from leaderboard_record where game_level = 'LEVEL_2'")).isZero();
		assertThat(nullableState("leaderboard_record", "game_level")).isEqualTo("YES");
		assertThat(hasConstraint("leaderboard_record", "leaderboard_record_game_mode_check")).isFalse();

		assertThat(count("select count(*) from location_game_session")).isEqualTo(locationSessionsBefore + 1);
		assertThat(count("select count(*) from population_game_session")).isEqualTo(populationSessionsBefore + 1);
		assertThat(count("select count(*) from leaderboard_record")).isEqualTo(leaderboardRecordsBefore + 2);

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

	private void ensureLegacyGameLevelColumns() {
		addColumnIfMissing("location_game_session", "game_level");
		addColumnIfMissing("population_game_session", "game_level");
		addColumnIfMissing("leaderboard_record", "game_level");
	}

	private void addColumnIfMissing(String tableName, String columnName) {
		if (count(
			"""
				select count(*)
				from information_schema.columns
				where lower(table_name) = lower('%s')
				  and lower(column_name) = lower('%s')
				""".formatted(tableName, columnName)
		) == 0) {
			jdbcTemplate.execute(
				"alter table " + tableName + " add column " + columnName + " varchar(20) default 'LEVEL_1'"
			);
		}
	}

	private void createLocationRun(Country country, boolean legacyLevelTwo, LocalDateTime finishedAt, String signature) {
		LocationGameSession session = locationGameSessionRepository.save(LocationGameSession.ready(signature, 1));
		LocationGameStage stage = locationGameStageRepository.save(LocationGameStage.create(session, 1, country));
		locationGameAttemptRepository.save(LocationGameAttempt.create(stage, 1, country, true, 3, finishedAt.minusSeconds(1)));
		LeaderboardRecord record = leaderboardRecordRepository.save(
			LeaderboardRecord.create(
				signature,
				session.getId(),
				LeaderboardGameMode.LOCATION,
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
		if (legacyLevelTwo) {
			jdbcTemplate.update("update location_game_session set game_level = 'LEVEL_2' where id = ?", session.getId());
			jdbcTemplate.update("update leaderboard_record set game_level = 'LEVEL_2' where id = ?", record.getId());
		}
	}

	private void createPopulationRun(Country country, boolean legacyLevelTwo, LocalDateTime finishedAt, String signature) {
		PopulationGameSession session = populationGameSessionRepository.save(PopulationGameSession.ready(signature, 1));
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
		LeaderboardRecord record = leaderboardRecordRepository.save(
			LeaderboardRecord.create(
				signature,
				session.getId(),
				LeaderboardGameMode.POPULATION,
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
		if (legacyLevelTwo) {
			jdbcTemplate.update("update population_game_session set game_level = 'LEVEL_2' where id = ?", session.getId());
			jdbcTemplate.update("update leaderboard_record set game_level = 'LEVEL_2' where id = ?", record.getId());
		}
	}

	private long count(String sql) {
		Long value = jdbcTemplate.queryForObject(sql, Long.class);
		return value == null ? 0L : value;
	}

	private String nullableState(String tableName, String columnName) {
		return jdbcTemplate.queryForObject(
			"""
				select is_nullable
				from information_schema.columns
				where lower(table_name) = lower(?)
				  and lower(column_name) = lower(?)
				""",
			String.class,
			tableName,
			columnName
		);
	}

	private void replaceLeaderboardGameModeCheckWithLegacySubset() {
		jdbcTemplate.execute("alter table leaderboard_record drop constraint if exists leaderboard_record_game_mode_check");
		jdbcTemplate.execute("""
			alter table leaderboard_record
			add constraint leaderboard_record_game_mode_check
			check (game_mode in ('LOCATION', 'POPULATION'))
			""");
	}

	private boolean hasConstraint(String tableName, String constraintName) {
		Integer count = jdbcTemplate.queryForObject(
			"""
				select count(*)
				from information_schema.table_constraints
				where lower(table_name) = lower(?)
				  and lower(constraint_name) = lower(?)
				""",
			Integer.class,
			tableName,
			constraintName
		);
		return count != null && count > 0;
	}
}
