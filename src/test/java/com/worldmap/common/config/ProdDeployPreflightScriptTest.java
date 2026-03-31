package com.worldmap.common.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ProdDeployPreflightScriptTest {

	private static final Path SCRIPT_PATH = Path.of("scripts/check_prod_deploy_preflight.py");
	private static final Path WORKFLOW_PATH = Path.of(".github/workflows/deploy-prod-ecs.yml");

	@Test
	void scriptFailsAndListsMissingVariablesWhenRepoVariablesAreEmpty() throws IOException, InterruptedException {
		assertThat(Files.exists(SCRIPT_PATH)).isTrue();
		assertThat(Files.exists(WORKFLOW_PATH)).isTrue();

		Path variablesJson = Files.createTempFile("worldmap-deploy-variables-empty-", ".json");
		Files.writeString(variablesJson, """
			{"variables":[],"total_count":0}
			""");
		Path reportPath = Files.createTempFile("worldmap-deploy-preflight-", ".md");

		ProcessBuilder processBuilder = new ProcessBuilder(
			"python3",
			SCRIPT_PATH.toString(),
			"--repo",
			"answndud/world_map_game",
			"--workflow",
			WORKFLOW_PATH.toString(),
			"--variables-json",
			variablesJson.toString(),
			"--report",
			reportPath.toString()
		);
		processBuilder.directory(Path.of(".").toFile());
		processBuilder.redirectErrorStream(true);

		Process process = processBuilder.start();
		String output = new String(process.getInputStream().readAllBytes());
		int exitCode = process.waitFor();

		assertThat(exitCode).isEqualTo(1);
		assertThat(output).contains("Deploy preflight failed.");
		String report = Files.readString(reportPath);
		assertThat(report)
			.contains("Ready: NO")
			.contains("AWS_REGION")
			.contains("ECS_CLUSTER")
			.contains("ADMIN_BOOTSTRAP_PASSWORD_PARAMETER_ARN");
	}

	@Test
	void scriptPassesWhenAllRequiredVariablesArePresent() throws IOException, InterruptedException {
		Path variablesJson = Files.createTempFile("worldmap-deploy-variables-full-", ".json");
		Files.writeString(variablesJson, """
			{
			  "variables": [
			    {"name":"AWS_REGION"},
			    {"name":"AWS_ACCOUNT_ID"},
			    {"name":"AWS_GITHUB_ACTIONS_ROLE_ARN"},
			    {"name":"ECR_REPOSITORY"},
			    {"name":"ECS_CLUSTER"},
			    {"name":"ECS_SERVICE"},
			    {"name":"ECS_EXECUTION_ROLE_ARN"},
			    {"name":"ECS_TASK_ROLE_ARN"},
			    {"name":"RDS_ENDPOINT"},
			    {"name":"ELASTICACHE_ENDPOINT"},
			    {"name":"CLOUDWATCH_LOG_GROUP"},
			    {"name":"SPRING_DATASOURCE_PASSWORD_SECRET_ARN"},
			    {"name":"ADMIN_BOOTSTRAP_PASSWORD_PARAMETER_ARN"}
			  ],
			  "total_count": 13
			}
			""");
		Path reportPath = Files.createTempFile("worldmap-deploy-preflight-ready-", ".md");

		ProcessBuilder processBuilder = new ProcessBuilder(
			"python3",
			SCRIPT_PATH.toString(),
			"--repo",
			"answndud/world_map_game",
			"--workflow",
			WORKFLOW_PATH.toString(),
			"--variables-json",
			variablesJson.toString(),
			"--report",
			reportPath.toString()
		);
		processBuilder.directory(Path.of(".").toFile());
		processBuilder.redirectErrorStream(true);

		Process process = processBuilder.start();
		String output = new String(process.getInputStream().readAllBytes());
		int exitCode = process.waitFor();

		assertThat(exitCode).isZero();
		assertThat(output).contains("Deploy preflight passed.");
		String report = Files.readString(reportPath);
		assertThat(report)
			.contains("Ready: YES")
			.contains("workflow_dispatch: enabled")
			.contains("ALB DNS")
			.doesNotContain("| AWS_REGION | missing |");
	}
}
