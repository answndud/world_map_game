package com.worldmap.common.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;

@Component
public class GameSessionStatusConstraintInitializer implements ApplicationRunner {

	private final JdbcTemplate jdbcTemplate;
	private final DataSource dataSource;

	public GameSessionStatusConstraintInitializer(JdbcTemplate jdbcTemplate, DataSource dataSource) {
		this.jdbcTemplate = jdbcTemplate;
		this.dataSource = dataSource;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		if (!isPostgreSql()) {
			return;
		}

		refreshConstraint("location_game_session", "location_game_session_status_check");
		refreshConstraint("population_game_session", "population_game_session_status_check");
	}

	private boolean isPostgreSql() throws Exception {
		try (var connection = dataSource.getConnection()) {
			String databaseProductName = connection.getMetaData().getDatabaseProductName();
			return databaseProductName != null && databaseProductName.toLowerCase().contains("postgresql");
		}
	}

	private void refreshConstraint(String tableName, String constraintName) {
		String sql = """
			DO $$
			BEGIN
			    IF EXISTS (
			        SELECT 1
			        FROM information_schema.tables
			        WHERE table_schema = 'public'
			          AND table_name = '%s'
			    ) THEN
			        EXECUTE 'ALTER TABLE %s DROP CONSTRAINT IF EXISTS %s';
			        EXECUTE 'ALTER TABLE %s ADD CONSTRAINT %s CHECK (status::text = ANY (ARRAY[''READY''::character varying, ''IN_PROGRESS''::character varying, ''FINISHED''::character varying, ''GAME_OVER''::character varying]::text[]))';
			    END IF;
			END $$;
			""".formatted(tableName, tableName, constraintName, tableName, constraintName);

		jdbcTemplate.execute(sql);
	}
}
