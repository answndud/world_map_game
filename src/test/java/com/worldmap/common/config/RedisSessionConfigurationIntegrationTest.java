package com.worldmap.common.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.session.FindByIndexNameSessionRepository;

@SpringBootTest(
	properties = {
		"spring.profiles.active=test,prod",
		"SPRING_DATASOURCE_URL=jdbc:h2:mem:sessionconfig;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
		"SPRING_DATASOURCE_USERNAME=sa",
		"SPRING_DATASOURCE_PASSWORD=",
		"SPRING_DATA_REDIS_HOST=localhost",
		"SPRING_DATA_REDIS_PORT=6379",
		"SPRING_DATA_REDIS_SSL_ENABLED=false"
	}
)
class RedisSessionConfigurationIntegrationTest {

	@Autowired
	private FindByIndexNameSessionRepository<?> sessionRepository;

	@Test
	void prodProfileEnablesRedisBackedSessionRepository() {
		assertThat(sessionRepository).isNotNull();
		assertThat(sessionRepository.getClass().getSimpleName()).containsIgnoringCase("redis");
	}
}
