package com.worldmap.recommendation.application;

import com.worldmap.recommendation.domain.RecommendationFeedback;
import com.worldmap.recommendation.domain.RecommendationFeedbackRepository;
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
}
