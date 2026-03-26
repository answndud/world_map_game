package com.worldmap.common.config;

import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(27)
public class GameLevelRollbackInitializer implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(GameLevelRollbackInitializer.class);

	private final JdbcTemplate jdbcTemplate;
	private final StringRedisTemplate stringRedisTemplate;
	private final String leaderboardKeyPrefix;

	public GameLevelRollbackInitializer(
		JdbcTemplate jdbcTemplate,
		StringRedisTemplate stringRedisTemplate,
		@Value("${worldmap.ranking.key-prefix:leaderboard}") String leaderboardKeyPrefix
	) {
		this.jdbcTemplate = jdbcTemplate;
		this.stringRedisTemplate = stringRedisTemplate;
		this.leaderboardKeyPrefix = leaderboardKeyPrefix;
	}

	@Override
	public void run(ApplicationArguments args) {
		boolean hasLocationGameLevel = hasColumn("location_game_session", "game_level");
		boolean hasPopulationGameLevel = hasColumn("population_game_session", "game_level");
		boolean hasLeaderboardGameLevel = hasColumn("leaderboard_record", "game_level");

		int deletedLocationAttempts = hasLocationGameLevel ? jdbcTemplate.update("""
			DELETE FROM location_game_attempt
			WHERE stage_id IN (
			    SELECT stage.id
			    FROM location_game_stage stage
			    JOIN location_game_session session ON stage.session_id = session.id
			    WHERE session.game_level = 'LEVEL_2'
			)
			""") : 0;
		int deletedLocationStages = hasLocationGameLevel ? jdbcTemplate.update("""
			DELETE FROM location_game_stage
			WHERE session_id IN (
			    SELECT id
			    FROM location_game_session
			    WHERE game_level = 'LEVEL_2'
			)
			""") : 0;
		int deletedLocationSessions = hasLocationGameLevel ? jdbcTemplate.update("""
			DELETE FROM location_game_session
			WHERE game_level = 'LEVEL_2'
			""") : 0;

		int deletedPopulationAttempts = hasPopulationGameLevel ? jdbcTemplate.update("""
			DELETE FROM population_game_attempt
			WHERE stage_id IN (
			    SELECT stage.id
			    FROM population_game_stage stage
			    JOIN population_game_session session ON stage.session_id = session.id
			    WHERE session.game_level = 'LEVEL_2'
			)
			""") : 0;
		int deletedPopulationStages = hasPopulationGameLevel ? jdbcTemplate.update("""
			DELETE FROM population_game_stage
			WHERE session_id IN (
			    SELECT id
			    FROM population_game_session
			    WHERE game_level = 'LEVEL_2'
			)
			""") : 0;
		int deletedPopulationSessions = hasPopulationGameLevel ? jdbcTemplate.update("""
			DELETE FROM population_game_session
			WHERE game_level = 'LEVEL_2'
			""") : 0;

		int deletedLeaderboardRecords = hasLeaderboardGameLevel ? jdbcTemplate.update("""
			DELETE FROM leaderboard_record
			WHERE game_level = 'LEVEL_2'
			""") : 0;

		Set<String> levelTwoKeys = stringRedisTemplate.keys(leaderboardKeyPrefix + ":*:*:l2*");
		if (levelTwoKeys != null && !levelTwoKeys.isEmpty()) {
			stringRedisTemplate.delete(levelTwoKeys);
		}

		log.info(
			"Rolled back Level 2 data. locationSessions={}, locationStages={}, locationAttempts={}, populationSessions={}, populationStages={}, populationAttempts={}, leaderboardRecords={}, redisKeys={}",
			deletedLocationSessions,
			deletedLocationStages,
			deletedLocationAttempts,
			deletedPopulationSessions,
			deletedPopulationStages,
			deletedPopulationAttempts,
			deletedLeaderboardRecords,
			levelTwoKeys == null ? 0 : levelTwoKeys.size()
		);
	}

	private boolean hasColumn(String tableName, String columnName) {
		Integer count = jdbcTemplate.queryForObject(
			"""
				SELECT count(*)
				FROM information_schema.columns
				WHERE lower(table_name) = lower(?)
				  AND lower(column_name) = lower(?)
				""",
			Integer.class,
			tableName,
			columnName
		);
		return count != null && count > 0;
	}
}
