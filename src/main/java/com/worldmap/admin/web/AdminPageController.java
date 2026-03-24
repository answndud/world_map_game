package com.worldmap.admin.web;

import com.worldmap.admin.application.AdminDashboardService;
import com.worldmap.recommendation.application.RecommendationFeedbackService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminPageController {

	private final AdminDashboardService adminDashboardService;
	private final RecommendationFeedbackService recommendationFeedbackService;

	public AdminPageController(
		AdminDashboardService adminDashboardService,
		RecommendationFeedbackService recommendationFeedbackService
	) {
		this.adminDashboardService = adminDashboardService;
		this.recommendationFeedbackService = recommendationFeedbackService;
	}

	@GetMapping
	public String dashboard(Model model) {
		model.addAttribute("dashboard", adminDashboardService.loadDashboard());
		return "admin/index";
	}

	@GetMapping("/recommendation/feedback")
	public String recommendationFeedback(Model model) {
		model.addAttribute("dashboard", adminDashboardService.loadDashboard());
		model.addAttribute("feedbackInsights", recommendationFeedbackService.summarizeByVersion());
		return "admin/recommendation-feedback";
	}
}
