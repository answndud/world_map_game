package com.worldmap.common.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class EcsTaskDefinitionTemplateTest {

	private static final Path TASK_DEFINITION_PATH = Path.of("deploy/ecs/task-definition.prod.sample.json");

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void taskDefinitionTemplateUsesSecretsForSensitiveValues() throws IOException {
		assertThat(Files.exists(TASK_DEFINITION_PATH)).isTrue();

		JsonNode root = objectMapper.readTree(Files.readString(TASK_DEFINITION_PATH));
		assertThat(root.path("family").asText()).isEqualTo("worldmap-prod");
		assertThat(root.path("requiresCompatibilities").toString()).contains("FARGATE");

		JsonNode container = root.path("containerDefinitions").get(0);
		Map<String, String> environment = toMap(container.path("environment"));
		Map<String, String> secrets = toMap(container.path("secrets"));

		assertThat(environment)
			.containsEntry("SPRING_PROFILES_ACTIVE", "prod")
			.containsEntry("WORLDMAP_DEMO_BOOTSTRAP_ENABLED", "false")
			.containsEntry("WORLDMAP_ADMIN_BOOTSTRAP_ENABLED", "true")
			.containsEntry("WORLDMAP_ADMIN_BOOTSTRAP_NICKNAME", "worldmap_admin")
			.containsEntry("SPRING_DATA_REDIS_SSL_ENABLED", "true")
			.containsEntry("JAVA_RUNTIME_OPTS", "-XX:MaxRAMPercentage=75.0");

		assertThat(secrets.keySet())
			.containsExactlyInAnyOrder(
				"SPRING_DATASOURCE_PASSWORD",
				"WORLDMAP_ADMIN_BOOTSTRAP_PASSWORD"
			);
		assertThat(secrets.get("SPRING_DATASOURCE_PASSWORD")).contains(":secretsmanager:");
		assertThat(secrets.get("WORLDMAP_ADMIN_BOOTSTRAP_PASSWORD")).contains(":ssm:");

		assertThat(intersection(environment.keySet(), secrets.keySet())).isEmpty();
	}

	private Map<String, String> toMap(JsonNode entries) {
		Map<String, String> result = new HashMap<>();
		for (JsonNode entry : entries) {
			result.put(entry.path("name").asText(), entry.path("value").asText(entry.path("valueFrom").asText()));
		}
		return result;
	}

	private Set<String> intersection(Set<String> left, Set<String> right) {
		Set<String> overlap = new HashSet<>(left);
		overlap.retainAll(right);
		return overlap;
	}
}
