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
	void prodProfileDefinesEcsReadyDefaults() throws IOException {
		Map<String, Object> properties = loadYaml("application-prod.yml");

		assertThat(asString(properties, "spring.docker.compose.enabled")).isEqualTo("false");
		assertThat(asString(properties, "spring.thymeleaf.cache")).isEqualTo("true");
		assertThat(asString(properties, "spring.jpa.hibernate.ddl-auto")).isEqualTo("update");
		assertThat(asString(properties, "spring.sql.init.mode")).isEqualTo("never");
		assertThat(asString(properties, "server.forward-headers-strategy")).isEqualTo("native");
		assertThat(asString(properties, "worldmap.demo.bootstrap.enabled")).isEqualTo("false");
		assertThat(asString(properties, "worldmap.ranking.key-prefix"))
			.isEqualTo("${WORLDMAP_RANKING_KEY_PREFIX:leaderboard}");
		assertThat(asString(properties, "spring.datasource.url")).isEqualTo("${SPRING_DATASOURCE_URL}");
		assertThat(asString(properties, "spring.datasource.username")).isEqualTo("${SPRING_DATASOURCE_USERNAME}");
		assertThat(asString(properties, "spring.datasource.password")).isEqualTo("${SPRING_DATASOURCE_PASSWORD}");
		assertThat(asString(properties, "spring.data.redis.host")).isEqualTo("${SPRING_DATA_REDIS_HOST}");
		assertThat(asString(properties, "spring.data.redis.port")).isEqualTo("${SPRING_DATA_REDIS_PORT:6379}");
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
