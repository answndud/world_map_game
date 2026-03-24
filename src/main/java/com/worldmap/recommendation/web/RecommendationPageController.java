package com.worldmap.recommendation.web;

import com.worldmap.recommendation.application.RecommendationQuestionCatalog;
import com.worldmap.recommendation.application.RecommendationSurveyService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/recommendation")
public class RecommendationPageController {

	private final RecommendationQuestionCatalog questionCatalog;
	private final RecommendationSurveyService recommendationSurveyService;

	public RecommendationPageController(
		RecommendationQuestionCatalog questionCatalog,
		RecommendationSurveyService recommendationSurveyService
	) {
		this.questionCatalog = questionCatalog;
		this.recommendationSurveyService = recommendationSurveyService;
	}

	@GetMapping("/survey")
	public String survey(Model model) {
		if (!model.containsAttribute("surveyForm")) {
			model.addAttribute("surveyForm", new RecommendationSurveyForm());
		}
		model.addAttribute("surveyQuestions", questionCatalog.questions());
		return "recommendation/survey";
	}

	@PostMapping("/survey")
	public String recommend(
		@Valid @ModelAttribute("surveyForm") RecommendationSurveyForm surveyForm,
		BindingResult bindingResult,
		Model model
	) {
		if (bindingResult.hasErrors()) {
			model.addAttribute("surveyQuestions", questionCatalog.questions());
			model.addAttribute("surveyErrorMessage", "모든 문항을 선택하면 서버가 가중치를 계산해 상위 3개 국가를 추천합니다.");
			return "recommendation/survey";
		}

		model.addAttribute("result", recommendationSurveyService.recommend(surveyForm.toAnswers()));
		return "recommendation/result";
	}

	@GetMapping("/feedback-insights")
	public String feedbackInsightsRedirect() {
		return "redirect:/admin/recommendation/feedback";
	}
}
