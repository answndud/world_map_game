package com.worldmap.common.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

class ProdProfileConfigTest {

	@Test
	void prodProfileDefinesDeployReadyDefaults() throws IOException {
		Map<String, Object> properties = loadYaml("application-prod.yml");

		assertThat(asString(properties, "spring.docker.compose.enabled")).isEqualTo("false");
		assertThat(asString(properties, "spring.thymeleaf.cache")).isEqualTo("true");
		assertThat(asString(properties, "spring.lifecycle.timeout-per-shutdown-phase")).isEqualTo("20s");
		assertThat(asString(properties, "spring.jpa.hibernate.ddl-auto")).isEqualTo("validate");
		assertThat(asString(properties, "spring.sql.init.mode")).isEqualTo("never");
		assertThat(asString(properties, "server.forward-headers-strategy")).isEqualTo("native");
		assertThat(asString(properties, "server.shutdown")).isEqualTo("graceful");
		assertThat(asString(properties, "server.servlet.session.timeout")).isEqualTo("14d");
		assertThat(asString(properties, "server.servlet.session.cookie.name")).isEqualTo("WMSESSION");
		assertThat(asString(properties, "server.servlet.session.cookie.http-only")).isEqualTo("true");
		assertThat(asString(properties, "server.servlet.session.cookie.same-site")).isEqualTo("lax");
		assertThat(asString(properties, "server.servlet.session.cookie.secure")).isEqualTo("true");
		assertThat(asString(properties, "management.endpoint.health.probes.enabled")).isEqualTo("true");
		assertThat(asString(properties, "management.endpoint.health.group.liveness.include"))
			.isEqualTo("livenessState,ping");
		assertThat(asString(properties, "management.endpoint.health.group.readiness.include"))
			.isEqualTo("readinessState,db,redis,ping");
		assertThat(asString(properties, "management.health.livenessstate.enabled")).isEqualTo("true");
		assertThat(asString(properties, "management.health.readinessstate.enabled")).isEqualTo("true");
		assertThat(asString(properties, "worldmap.legacy.rollback.enabled")).isEqualTo("false");
		assertThat(asString(properties, "worldmap.demo.bootstrap.enabled")).isEqualTo("false");
		assertThat(asString(properties, "worldmap.ranking.key-prefix"))
			.isEqualTo("${WORLDMAP_RANKING_KEY_PREFIX:leaderboard}");
		assertThat(asString(properties, "spring.datasource.url")).isEqualTo("${SPRING_DATASOURCE_URL}");
		assertThat(asString(properties, "spring.datasource.username")).isEqualTo("${SPRING_DATASOURCE_USERNAME}");
		assertThat(asString(properties, "spring.datasource.password")).isEqualTo("${SPRING_DATASOURCE_PASSWORD}");
		assertThat(asString(properties, "spring.data.redis.host")).isEqualTo("${SPRING_DATA_REDIS_HOST:}");
		assertThat(asString(properties, "spring.data.redis.port")).isEqualTo("${SPRING_DATA_REDIS_PORT:6379}");
		assertThat(asString(properties, "spring.data.redis.username"))
			.isEqualTo("${SPRING_DATA_REDIS_USERNAME:}");
		assertThat(asString(properties, "spring.data.redis.password"))
			.isEqualTo("${SPRING_DATA_REDIS_PASSWORD:}");
		assertThat(asString(properties, "spring.data.redis.ssl.enabled"))
			.isEqualTo("${SPRING_DATA_REDIS_SSL_ENABLED:false}");
		assertThat(asString(properties, "worldmap.admin.bootstrap.enabled"))
			.isEqualTo("${WORLDMAP_ADMIN_BOOTSTRAP_ENABLED:false}");
	}

	private Map<String, Object> loadYaml(String resourcePath) throws IOException {
		YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
		List<PropertySource<?>> loadedSources = loader.load("test-" + resourcePath, new ClassPathResource(resourcePath));
		Map<String, Object> merged = new HashMap<>();
		for (PropertySource<?> source : loadedSources) {
			if (source.getSource() instanceof Map<?, ?> sourceMap) {
				sourceMap.forEach((key, value) -> merged.put(String.valueOf(key), value));
			}
		}
		return merged;
	}

	private String asString(Map<String, Object> properties, String key) {
		return String.valueOf(properties.get(key));
	}
}
