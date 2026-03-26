package com.worldmap.admin;

import static org.assertj.core.api.Assertions.assertThat;

import com.worldmap.admin.application.AdminPersonaBaselineScenarioView;
import com.worldmap.admin.application.AdminPersonaBaselineService;
import com.worldmap.admin.application.AdminPersonaBaselineView;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class AdminPersonaBaselineServiceIntegrationTest {

	@Autowired
	private AdminPersonaBaselineService adminPersonaBaselineService;

	@Test
	void loadBaselineDerivesWeakAndActiveSignalScenariosFromCurrentEngine() {
		AdminPersonaBaselineView baseline = adminPersonaBaselineService.loadBaseline();

		assertThat(baseline.totalScenarioCount()).isEqualTo(18);
		assertThat(baseline.matchedScenarioCount()).isEqualTo(18);
		assertThat(baseline.weakScenarioCount()).isZero();
		assertThat(baseline.activeSignalScenarioCount()).isEqualTo(4);
		assertThat(baseline.anchorDriftScenarioCount()).isZero();

		for (AdminPersonaBaselineScenarioView weakScenario : baseline.weakScenarios()) {
			assertThat(weakScenario.currentTopCandidates())
				.as("weak scenario should miss all expected candidates: %s", weakScenario.scenarioId())
				.doesNotContainAnyElementsOf(weakScenario.expectedTopCandidates());
		}

		Set<String> anchorDriftScenarioIds = baseline.anchorDriftScenarios().stream()
			.map(AdminPersonaBaselineScenarioView::scenarioId)
			.collect(Collectors.toSet());

		assertThat(anchorDriftScenarioIds).isEmpty();

		Set<String> activeScenarioIds = baseline.activeSignalScenarios().stream()
			.map(AdminPersonaBaselineScenarioView::scenarioId)
			.collect(Collectors.toSet());

		assertThat(activeScenarioIds).containsExactlyInAnyOrder("P15", "P16", "P17", "P18");
	}
}
