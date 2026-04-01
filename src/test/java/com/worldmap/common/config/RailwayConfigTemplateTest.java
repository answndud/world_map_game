package com.worldmap.common.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class RailwayConfigTemplateTest {

	private static final Path RAILWAY_TOML_PATH = Path.of("railway.toml");

	@Test
	void railwayTomlPinsRailpackBuildAndReadinessHealthCheck() throws IOException {
		assertThat(Files.exists(RAILWAY_TOML_PATH)).isTrue();

		String config = Files.readString(RAILWAY_TOML_PATH);
		assertThat(config)
			.contains("builder = \"RAILPACK\"")
			.contains("buildCommand = \"./gradlew --no-daemon bootJar\"")
			.contains("startCommand = \"java -Dserver.port=$PORT -jar build/libs/worldmap.jar\"")
			.contains("healthcheckPath = \"/actuator/health/readiness\"")
			.contains("restartPolicyType = \"ON_FAILURE\"")
			.contains("numReplicas = 1");
	}
}
