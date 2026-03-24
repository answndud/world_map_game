package com.worldmap.admin.application;

import java.util.List;
import com.worldmap.stats.application.ServiceActivityView;

public record AdminDashboardView(
	String currentSurveyVersion,
	String currentEngineVersion,
	ServiceActivityView activity,
	int recommendationQuestionCount,
	int recommendationCandidateCount,
	long totalFeedbackResponses,
	int trackedVersionCount,
	double overallAverageSatisfaction,
	List<AdminDashboardRouteView> adminRoutes,
	List<AdminDashboardFocusView> focusItems
) {
}
