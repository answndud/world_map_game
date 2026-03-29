package com.worldmap.common.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RenderEcsTaskDefinitionScriptTest {

	private static final Path SCRIPT_PATH = Path.of("scripts/render_ecs_task_definition.py");
	private static final Path INPUT_PATH = Path.of("deploy/ecs/task-definition.prod.sample.json");

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void scriptRendersConcreteTaskDefinitionFromTemplate() throws IOException, InterruptedException {
		assertThat(Files.exists(SCRIPT_PATH)).isTrue();
		assertThat(Files.exists(INPUT_PATH)).isTrue();

		Path outputPath = Files.createTempFile("worldmap-task-definition-", ".json");
		ProcessBuilder processBuilder = new ProcessBuilder(
			"python3",
			SCRIPT_PATH.toString(),
			"--input",
			INPUT_PATH.toString(),
			"--output",
			outputPath.toString()
		);
		processBuilder.directory(Path.of(".").toFile());
		processBuilder.environment().putAll(environment());
		processBuilder.redirectErrorStream(true);

		Process process = processBuilder.start();
		String output = new String(process.getInputStream().readAllBytes());
		int exitCode = process.waitFor();

		assertThat(exitCode).withFailMessage(output).isZero();

		JsonNode root = objectMapper.readTree(Files.readString(outputPath));
		JsonNode container = root.path("containerDefinitions").get(0);
		Map<String, String> environment = toMap(container.path("environment"));
		Map<String, String> secrets = toMap(container.path("secrets"), "valueFrom");

		assertThat(root.path("executionRoleArn").asText())
			.isEqualTo("arn:aws:iam::123456789012:role/worldmap-ecs-execution-role");
		assertThat(root.path("taskRoleArn").asText())
			.isEqualTo("arn:aws:iam::123456789012:role/worldmap-ecs-task-role");
		assertThat(container.path("image").asText())
			.isEqualTo("123456789012.dkr.ecr.ap-northeast-2.amazonaws.com/worldmap:abcdef123456");
		assertThat(environment)
			.containsEntry("SPRING_DATASOURCE_URL", "jdbc:postgresql://worldmap-rds.example.amazonaws.com:5432/worldmap")
			.containsEntry("SPRING_DATA_REDIS_HOST", "worldmap-cache.example.amazonaws.com");
		assertThat(secrets)
			.containsEntry(
				"SPRING_DATASOURCE_PASSWORD",
				"arn:aws:secretsmanager:ap-northeast-2:123456789012:secret:worldmap/prod/db-password"
			)
			.containsEntry(
				"WORLDMAP_ADMIN_BOOTSTRAP_PASSWORD",
				"arn:aws:ssm:ap-northeast-2:123456789012:parameter/worldmap/prod/admin-bootstrap-password"
			);
		assertThat(Files.readString(outputPath)).doesNotContain("<ACCOUNT_ID>").doesNotContain("<REGION>");
	}

	private Map<String, String> environment() {
		Map<String, String> environment = new HashMap<>();
		environment.put("AWS_ACCOUNT_ID", "123456789012");
		environment.put("AWS_REGION", "ap-northeast-2");
		environment.put("IMAGE_URI", "123456789012.dkr.ecr.ap-northeast-2.amazonaws.com/worldmap:abcdef123456");
		environment.put("ECS_EXECUTION_ROLE_ARN", "arn:aws:iam::123456789012:role/worldmap-ecs-execution-role");
		environment.put("ECS_TASK_ROLE_ARN", "arn:aws:iam::123456789012:role/worldmap-ecs-task-role");
		environment.put("RDS_ENDPOINT", "worldmap-rds.example.amazonaws.com");
		environment.put("ELASTICACHE_ENDPOINT", "worldmap-cache.example.amazonaws.com");
		environment.put("CLOUDWATCH_LOG_GROUP", "/ecs/worldmap-prod");
		environment.put(
			"SPRING_DATASOURCE_PASSWORD_SECRET_ARN",
			"arn:aws:secretsmanager:ap-northeast-2:123456789012:secret:worldmap/prod/db-password"
		);
		environment.put(
			"ADMIN_BOOTSTRAP_PASSWORD_PARAMETER_ARN",
			"arn:aws:ssm:ap-northeast-2:123456789012:parameter/worldmap/prod/admin-bootstrap-password"
		);
		return environment;
	}

	private Map<String, String> toMap(JsonNode entries) {
		return toMap(entries, "value");
	}

	private Map<String, String> toMap(JsonNode entries, String fieldName) {
		Map<String, String> result = new HashMap<>();
		for (JsonNode entry : entries) {
			result.put(entry.path("name").asText(), entry.path(fieldName).asText());
		}
		return result;
	}
}
