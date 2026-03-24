package com.worldmap.admin.application;

import java.util.List;

public record AdminDashboardView(
	String currentSurveyVersion,
	String currentEngineVersion,
	int recommendationQuestionCount,
	int recommendationCandidateCount,
	long totalFeedbackResponses,
	int trackedVersionCount,
	double overallAverageSatisfaction,
	List<AdminDashboardRouteView> adminRoutes,
	List<AdminDashboardFocusView> focusItems
) {
}
