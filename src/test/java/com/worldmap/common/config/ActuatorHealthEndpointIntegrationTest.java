package com.worldmap.common.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
	webEnvironment = SpringBootTest.WebEnvironment.MOCK,
	properties = {
		"spring.profiles.active=test,prod",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"SPRING_DATASOURCE_URL=jdbc:h2:mem:actuator;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
		"SPRING_DATASOURCE_USERNAME=sa",
		"SPRING_DATASOURCE_PASSWORD=",
		"SPRING_DATA_REDIS_HOST=localhost",
		"SPRING_DATA_REDIS_PORT=6379",
		"SPRING_DATA_REDIS_SSL_ENABLED=false"
	}
)
@AutoConfigureMockMvc
class ActuatorHealthEndpointIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void exposesHealthAndProbeEndpoints() throws Exception {
		mockMvc.perform(get("/actuator/health"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").exists());

		mockMvc.perform(get("/actuator/health/liveness"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").exists());

		mockMvc.perform(get("/actuator/health/readiness"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").exists());
	}
}
