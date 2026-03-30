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

class BrowserSmokeProfileConfigTest {

	@Test
	void browserSmokeProfileDisablesLegacyRollbackAndPointsRedisAwayFromDefaultPort() throws IOException {
		Map<String, Object> properties = loadYaml("application-browser-smoke.yml");

		assertThat(asString(properties, "worldmap.legacy.rollback.enabled")).isEqualTo("false");
		assertThat(asString(properties, "spring.data.redis.host")).isEqualTo("127.0.0.1");
		assertThat(asString(properties, "spring.data.redis.port")).isEqualTo("6390");
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
