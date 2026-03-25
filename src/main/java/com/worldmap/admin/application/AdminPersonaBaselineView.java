package com.worldmap.admin.application;

import java.util.List;

public record AdminPersonaBaselineView(
	int totalScenarioCount,
	int matchedScenarioCount,
	int weakScenarioCount,
	int activeSignalScenarioCount,
	int anchorDriftScenarioCount,
	List<AdminPersonaBaselineScenarioView> weakScenarios,
	List<AdminPersonaBaselineScenarioView> anchorDriftScenarios,
	List<AdminPersonaBaselineScenarioView> activeSignalScenarios
) {
}
