package com.worldmap.recommendation.web;

import com.worldmap.auth.application.AdminAccessGuard;
import com.worldmap.auth.application.AdminAccessGuard.AdminAccessStatus;
import com.worldmap.recommendation.application.RecommendationFeedbackContext;
import com.worldmap.recommendation.application.RecommendationFeedbackInsightsView;
import com.worldmap.recommendation.application.RecommendationFeedbackService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/recommendation")
public class RecommendationFeedbackApiController {

	private final RecommendationFeedbackService recommendationFeedbackService;
	private final RecommendationFeedbackSessionStore recommendationFeedbackSessionStore;
	private final AdminAccessGuard adminAccessGuard;

	public RecommendationFeedbackApiController(
		RecommendationFeedbackService recommendationFeedbackService,
		RecommendationFeedbackSessionStore recommendationFeedbackSessionStore,
		AdminAccessGuard adminAccessGuard
	) {
		this.recommendationFeedbackService = recommendationFeedbackService;
		this.recommendationFeedbackSessionStore = recommendationFeedbackSessionStore;
		this.adminAccessGuard = adminAccessGuard;
	}

	@PostMapping("/feedback")
	@ResponseStatus(HttpStatus.CREATED)
	public RecommendationFeedbackSavedResponse recordFeedback(
		@Valid @RequestBody RecommendationFeedbackRequest request,
		HttpSession httpSession
	) {
		RecommendationFeedbackContext feedbackContext = recommendationFeedbackSessionStore.consume(
			httpSession,
			request.getFeedbackToken()
		).orElseThrow(() -> new IllegalArgumentException("추천 결과 컨텍스트가 만료되었거나 올바르지 않습니다."));
		Long feedbackId = recommendationFeedbackService.record(request.toSubmission(feedbackContext));
		return new RecommendationFeedbackSavedResponse(
			feedbackId,
			request.getSatisfactionScore(),
			feedbackContext.surveyVersion(),
			feedbackContext.engineVersion()
		);
	}

	@GetMapping("/feedback/summary")
	public RecommendationFeedbackInsightsView feedbackSummary(HttpServletRequest request) {
		requireAdmin(request);
		return recommendationFeedbackService.summarizeByVersion();
	}

	private void requireAdmin(HttpServletRequest request) {
		if (adminAccessGuard.authorize(request) != AdminAccessStatus.ALLOWED) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "관리자만 접근할 수 있습니다.");
		}
	}
}
