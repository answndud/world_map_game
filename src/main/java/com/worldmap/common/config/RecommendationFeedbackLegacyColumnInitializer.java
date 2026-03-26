package com.worldmap.common.config;

import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(25)
public class RecommendationFeedbackLegacyColumnInitializer implements ApplicationRunner {

	private static final List<String> LEGACY_COLUMNS = List.of(
		"budget_preference",
		"english_importance",
		"priority_focus"
	);

	private final JdbcTemplate jdbcTemplate;

	public RecommendationFeedbackLegacyColumnInitializer(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public void run(ApplicationArguments args) {
		for (String columnName : LEGACY_COLUMNS) {
			relaxLegacyNotNullColumn(columnName);
		}
	}

	private void relaxLegacyNotNullColumn(String columnName) {
		Integer count = jdbcTemplate.queryForObject(
			"""
				SELECT COUNT(*)
				FROM information_schema.columns
				WHERE LOWER(table_name) = 'recommendation_feedback'
				  AND LOWER(column_name) = LOWER(?)
				""",
			Integer.class,
			columnName
		);

		if (count != null && count > 0) {
			jdbcTemplate.execute("ALTER TABLE recommendation_feedback ALTER COLUMN %s DROP NOT NULL".formatted(columnName));
		}
	}
}
