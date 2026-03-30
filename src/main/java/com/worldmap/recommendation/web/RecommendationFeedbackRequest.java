package com.worldmap.recommendation.web;

import com.worldmap.recommendation.application.RecommendationFeedbackSubmission;
import com.worldmap.recommendation.application.RecommendationFeedbackContext;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RecommendationFeedbackRequest {

	@NotBlank(message = "feedbackToken은 비어 있을 수 없습니다.")
	private String feedbackToken;

	@NotNull(message = "만족도 점수를 선택해주세요.")
	@Min(value = 1, message = "만족도 점수는 1점 이상이어야 합니다.")
	@Max(value = 5, message = "만족도 점수는 5점 이하여야 합니다.")
	private Integer satisfactionScore;

	public RecommendationFeedbackSubmission toSubmission(RecommendationFeedbackContext context) {
		return new RecommendationFeedbackSubmission(
			context.surveyVersion(),
			context.engineVersion(),
			satisfactionScore,
			context.answers()
		);
	}

	public String getFeedbackToken() {
		return feedbackToken;
	}

	public void setFeedbackToken(String feedbackToken) {
		this.feedbackToken = feedbackToken;
	}

	public Integer getSatisfactionScore() {
		return satisfactionScore;
	}

	public void setSatisfactionScore(Integer satisfactionScore) {
		this.satisfactionScore = satisfactionScore;
	}
}
