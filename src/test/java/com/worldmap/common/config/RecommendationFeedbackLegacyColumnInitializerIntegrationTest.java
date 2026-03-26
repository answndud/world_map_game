package com.worldmap.common.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.worldmap.recommendation.application.RecommendationSurveyService;
import com.worldmap.recommendation.domain.RecommendationFeedback;
import com.worldmap.recommendation.domain.RecommendationFeedbackRepository;
import com.worldmap.recommendation.domain.RecommendationSurveyAnswers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class RecommendationFeedbackLegacyColumnInitializerIntegrationTest {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private RecommendationFeedbackLegacyColumnInitializer initializer;

	@Autowired
	private RecommendationFeedbackRepository recommendationFeedbackRepository;

	@Test
	void initializerRelaxesLegacyRecommendationFeedbackColumnsAndAllowsCurrentInsert() {
		addLegacyColumn("budget_preference", "LOW");
		addLegacyColumn("english_importance", "HIGH");
		addLegacyColumn("priority_focus", "SAFETY");

		initializer.run(new DefaultApplicationArguments(new String[0]));

		assertThat(nullableState("budget_preference")).isEqualTo("YES");
		assertThat(nullableState("english_importance")).isEqualTo("YES");
		assertThat(nullableState("priority_focus")).isEqualTo("YES");

		RecommendationFeedback feedback = recommendationFeedbackRepository.save(
			RecommendationFeedback.create(
				RecommendationSurveyService.SURVEY_VERSION,
				RecommendationSurveyService.ENGINE_VERSION,
				5,
				new RecommendationSurveyAnswers(
					RecommendationSurveyAnswers.ClimatePreference.MILD,
					RecommendationSurveyAnswers.SeasonStylePreference.BALANCED,
					RecommendationSurveyAnswers.SeasonTolerance.MEDIUM,
					RecommendationSurveyAnswers.PacePreference.BALANCED,
					RecommendationSurveyAnswers.CrowdPreference.BALANCED,
					RecommendationSurveyAnswers.CostQualityPreference.BALANCED,
					RecommendationSurveyAnswers.HousingPreference.BALANCED,
					RecommendationSurveyAnswers.EnvironmentPreference.MIXED,
					RecommendationSurveyAnswers.MobilityPreference.BALANCED,
					RecommendationSurveyAnswers.EnglishSupportNeed.MEDIUM,
					RecommendationSurveyAnswers.NewcomerSupportNeed.MEDIUM,
					RecommendationSurveyAnswers.ImportanceLevel.HIGH,
					RecommendationSurveyAnswers.ImportanceLevel.MEDIUM,
					RecommendationSurveyAnswers.ImportanceLevel.MEDIUM,
					RecommendationSurveyAnswers.ImportanceLevel.HIGH,
					RecommendationSurveyAnswers.ImportanceLevel.MEDIUM,
					RecommendationSurveyAnswers.ImportanceLevel.MEDIUM,
					RecommendationSurveyAnswers.WorkLifePreference.BALANCED,
					RecommendationSurveyAnswers.SettlementPreference.BALANCED,
					RecommendationSurveyAnswers.FutureBasePreference.BALANCED
				)
			)
		);

		assertThat(feedback.getId()).isNotNull();
	}

	private void addLegacyColumn(String columnName, String defaultValue) {
		jdbcTemplate.execute("ALTER TABLE recommendation_feedback ADD COLUMN IF NOT EXISTS %s varchar(30)".formatted(columnName));
		jdbcTemplate.execute(
			"UPDATE recommendation_feedback SET %s = '%s' WHERE %s IS NULL".formatted(columnName, defaultValue, columnName)
		);
		jdbcTemplate.execute("ALTER TABLE recommendation_feedback ALTER COLUMN %s SET NOT NULL".formatted(columnName));
	}

	private String nullableState(String columnName) {
		return jdbcTemplate.queryForObject(
			"""
				SELECT is_nullable
				FROM information_schema.columns
				WHERE LOWER(table_name) = 'recommendation_feedback'
				  AND LOWER(column_name) = LOWER(?)
				""",
			String.class,
			columnName
		);
	}
}
