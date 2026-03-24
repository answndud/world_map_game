package com.worldmap.recommendation.web;

import com.worldmap.recommendation.application.RecommendationFeedbackService;
import com.worldmap.recommendation.application.RecommendationFeedbackInsightsView;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recommendation")
public class RecommendationFeedbackApiController {

	private final RecommendationFeedbackService recommendationFeedbackService;

	public RecommendationFeedbackApiController(RecommendationFeedbackService recommendationFeedbackService) {
		this.recommendationFeedbackService = recommendationFeedbackService;
	}

	@PostMapping("/feedback")
	@ResponseStatus(HttpStatus.CREATED)
	public RecommendationFeedbackSavedResponse recordFeedback(
		@Valid @RequestBody RecommendationFeedbackRequest request
	) {
		Long feedbackId = recommendationFeedbackService.record(request.toSubmission());
		return new RecommendationFeedbackSavedResponse(
			feedbackId,
			request.getSatisfactionScore(),
			request.getSurveyVersion(),
			request.getEngineVersion()
		);
	}

	@GetMapping("/feedback/summary")
	public RecommendationFeedbackInsightsView feedbackSummary() {
		return recommendationFeedbackService.summarizeByVersion();
	}
}
