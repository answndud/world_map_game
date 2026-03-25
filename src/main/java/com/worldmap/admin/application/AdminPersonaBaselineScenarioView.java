package com.worldmap.admin.application;

import java.util.List;

public record AdminPersonaBaselineScenarioView(
	String scenarioId,
	String title,
	String expectedAnchorCandidate,
	String currentAnchorCandidate,
	List<String> expectedTopCandidates,
	List<String> currentTopCandidates,
	String focusPoint
) {
}
