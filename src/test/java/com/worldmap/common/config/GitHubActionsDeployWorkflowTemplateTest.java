package com.worldmap.common.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class GitHubActionsDeployWorkflowTemplateTest {

	private static final Path WORKFLOW_PATH = Path.of(".github/workflows/deploy-prod-ecs.yml");

	@Test
	void workflowContainsCoreEcsDeploymentSteps() throws IOException {
		assertThat(Files.exists(WORKFLOW_PATH)).isTrue();

		String workflow = Files.readString(WORKFLOW_PATH);
		assertThat(workflow)
			.contains("workflow_dispatch:")
			.contains("id-token: write")
			.contains("actions/setup-java@v4")
			.contains("./gradlew test")
			.contains("aws-actions/configure-aws-credentials@v4")
			.contains("aws-actions/amazon-ecr-login@v2")
			.contains("scripts/render_ecs_task_definition.py")
			.contains("aws-actions/amazon-ecs-deploy-task-definition@v2");
	}
}
