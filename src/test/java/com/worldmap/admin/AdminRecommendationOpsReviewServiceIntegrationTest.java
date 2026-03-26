package com.worldmap.admin;

import static org.assertj.core.api.Assertions.assertThat;

import com.worldmap.admin.application.AdminRecommendationOpsReviewService;
import com.worldmap.admin.application.AdminRecommendationOpsReviewView;
import com.worldmap.recommendation.application.RecommendationSurveyService;
import com.worldmap.recommendation.domain.RecommendationFeedback;
import com.worldmap.recommendation.domain.RecommendationFeedbackRepository;
import com.worldmap.recommendation.domain.RecommendationSurveyAnswers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class AdminRecommendationOpsReviewServiceIntegrationTest {

	@Autowired
	private AdminRecommendationOpsReviewService adminRecommendationOpsReviewService;

	@Autowired
	private RecommendationFeedbackRepository recommendationFeedbackRepository;

	@BeforeEach
	void setUp() {
		recommendationFeedbackRepository.deleteAll();
	}

	@Test
	void loadReviewPrioritizesCollectingFeedbackWhenCurrentVersionSampleIsSmall() {
		recommendationFeedbackRepository.save(createFeedback(5));
		recommendationFeedbackRepository.save(createFeedback(4));

			AdminRecommendationOpsReviewView review = adminRecommendationOpsReviewService.loadReview();

			assertThat(review.currentVersionResponseCount()).isEqualTo(2);
			assertThat(review.baselineMatchedScenarioCount()).isEqualTo(18);
			assertThat(review.anchorDriftScenarioCount()).isEqualTo(1);
			assertThat(review.priorityActionTitle()).isEqualTo("현재 버전 피드백 더 수집");
			assertThat(review.priorityScenarioIds()).containsExactly("P07");
	}

	@Test
	void loadReviewPrioritizesRankDriftWhenCurrentVersionSampleIsEnough() {
		recommendationFeedbackRepository.save(createFeedback(5));
		recommendationFeedbackRepository.save(createFeedback(5));
		recommendationFeedbackRepository.save(createFeedback(4));
		recommendationFeedbackRepository.save(createFeedback(5));
		recommendationFeedbackRepository.save(createFeedback(4));

		AdminRecommendationOpsReviewView review = adminRecommendationOpsReviewService.loadReview();

			assertThat(review.currentVersionResponseCount()).isEqualTo(5);
			assertThat(review.currentVersionAverageSatisfaction()).isGreaterThanOrEqualTo(4.0);
			assertThat(review.priorityActionTitle()).isEqualTo("rank drift 줄이기");
			assertThat(review.priorityScenarioIds()).containsExactly("P07");
	}

	private RecommendationFeedback createFeedback(int score) {
		return RecommendationFeedback.create(
			RecommendationSurveyService.SURVEY_VERSION,
			RecommendationSurveyService.ENGINE_VERSION,
			score,
			new RecommendationSurveyAnswers(
				RecommendationSurveyAnswers.ClimatePreference.WARM,
				RecommendationSurveyAnswers.SeasonStylePreference.STABLE,
				RecommendationSurveyAnswers.SeasonTolerance.MEDIUM,
				RecommendationSurveyAnswers.PacePreference.BALANCED,
				RecommendationSurveyAnswers.CrowdPreference.BALANCED,
				RecommendationSurveyAnswers.CostQualityPreference.VALUE_FIRST,
				RecommendationSurveyAnswers.HousingPreference.SPACE_FIRST,
				RecommendationSurveyAnswers.EnvironmentPreference.CITY,
				RecommendationSurveyAnswers.MobilityPreference.BALANCED,
				RecommendationSurveyAnswers.EnglishSupportNeed.MEDIUM,
				RecommendationSurveyAnswers.NewcomerSupportNeed.MEDIUM,
				RecommendationSurveyAnswers.ImportanceLevel.LOW,
				RecommendationSurveyAnswers.ImportanceLevel.LOW,
				RecommendationSurveyAnswers.ImportanceLevel.MEDIUM,
				RecommendationSurveyAnswers.ImportanceLevel.HIGH,
				RecommendationSurveyAnswers.ImportanceLevel.MEDIUM,
				RecommendationSurveyAnswers.ImportanceLevel.MEDIUM,
				RecommendationSurveyAnswers.WorkLifePreference.BALANCED,
				RecommendationSurveyAnswers.SettlementPreference.BALANCED,
				RecommendationSurveyAnswers.FutureBasePreference.BALANCED
			)
		);
	}
}
