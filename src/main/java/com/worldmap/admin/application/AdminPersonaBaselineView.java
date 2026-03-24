package com.worldmap.admin.application;

import java.util.List;

public record AdminPersonaBaselineView(
	int totalScenarioCount,
	int matchedScenarioCount,
	int weakScenarioCount,
	int activeSignalScenarioCount,
	List<AdminPersonaBaselineScenarioView> weakScenarios,
	List<AdminPersonaBaselineScenarioView> activeSignalScenarios
) {
}
