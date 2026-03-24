package com.worldmap.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(HomeController.class)
class HomeControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void homePageRenders() throws Exception {
		mockMvc.perform(get("/"))
			.andExpect(status().isOk())
			.andExpect(view().name("home"))
			.andExpect(model().attributeExists("modeCards"))
			.andExpect(model().attributeExists("principles"))
			.andExpect(model().attributeExists("roadmap"))
			.andExpect(content().string(containsString("플레이 방식")))
			.andExpect(content().string(containsString(">My Page<")))
			.andExpect(content().string(not(containsString(">Location<"))))
			.andExpect(content().string(not(containsString(">Population<"))))
			.andExpect(content().string(not(containsString("오늘의 추천 플레이"))))
			.andExpect(content().string(not(containsString("Spring Boot 3 Game Platform"))))
			.andExpect(content().string(not(containsString("Current Build"))))
			.andExpect(content().string(not(containsString("ORBIT 0.4"))))
			.andExpect(content().string(not(containsString("현재 로드맵"))));
	}
}
