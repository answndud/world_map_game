package com.worldmap.common.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class DemoLiteVerifyWorkflowTemplateTest {

	private static final Path WORKFLOW_PATH = Path.of(".github/workflows/demo-lite-verify.yml");

	@Test
	void workflowContainsDemoLiteBuildAndManualPublicSmokeSteps() throws IOException {
		assertThat(Files.exists(WORKFLOW_PATH)).isTrue();

		String workflow = Files.readString(WORKFLOW_PATH);
		assertThat(workflow)
			.contains("name: demo-lite-verify")
			.contains("workflow_dispatch:")
			.contains("node-version-file: demo-lite/.node-version")
			.contains("npm ci")
			.contains("npm test")
			.contains("npm run build")
			.contains("npm run verify:pages")
			.contains("npm run smoke:public --");
	}
}
