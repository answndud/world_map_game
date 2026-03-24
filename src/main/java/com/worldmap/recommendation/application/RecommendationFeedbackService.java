package com.worldmap.recommendation.application;

import com.worldmap.recommendation.domain.RecommendationFeedback;
import com.worldmap.recommendation.domain.RecommendationFeedbackRepository;
import com.worldmap.recommendation.domain.RecommendationFeedbackVersionSummaryProjection;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecommendationFeedbackService {

	private final RecommendationFeedbackRepository recommendationFeedbackRepository;

	public RecommendationFeedbackService(RecommendationFeedbackRepository recommendationFeedbackRepository) {
		this.recommendationFeedbackRepository = recommendationFeedbackRepository;
	}

	@Transactional
	public Long record(RecommendationFeedbackSubmission submission) {
		RecommendationFeedback feedback = RecommendationFeedback.create(
			submission.surveyVersion(),
			submission.engineVersion(),
			submission.satisfactionScore(),
			submission.answers()
		);
		return recommendationFeedbackRepository.save(feedback).getId();
	}

	@Transactional(readOnly = true)
	public RecommendationFeedbackInsightsView summarizeByVersion() {
		List<RecommendationFeedbackSummaryView> versionSummaries = recommendationFeedbackRepository.summarizeByVersion()
			.stream()
			.map(this::toSummaryView)
			.toList();

		long totalResponses = versionSummaries.stream()
			.mapToLong(RecommendationFeedbackSummaryView::responseCount)
			.sum();

		double totalScore = versionSummaries.stream()
			.mapToDouble(summary -> summary.averageSatisfaction() * summary.responseCount())
			.sum();

		double overallAverageSatisfaction = totalResponses == 0 ? 0.0 : totalScore / totalResponses;

		return new RecommendationFeedbackInsightsView(
			totalResponses,
			versionSummaries.size(),
			overallAverageSatisfaction,
			versionSummaries
		);
	}

	private RecommendationFeedbackSummaryView toSummaryView(RecommendationFeedbackVersionSummaryProjection projection) {
		return new RecommendationFeedbackSummaryView(
			projection.getSurveyVersion(),
			projection.getEngineVersion(),
			projection.getResponseCount(),
			projection.getAverageSatisfaction(),
			projection.getScore1Count(),
			projection.getScore2Count(),
			projection.getScore3Count(),
			projection.getScore4Count(),
			projection.getScore5Count(),
			projection.getLastSubmittedAt()
		);
	}
}
