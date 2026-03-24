package com.worldmap.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MyPageController.class)
class MyPageControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void myPageRendersPlaceholderShell() throws Exception {
		mockMvc.perform(get("/mypage"))
			.andExpect(status().isOk())
			.andExpect(view().name("mypage"))
			.andExpect(content().string(containsString("내 프로필")))
			.andExpect(content().string(containsString(">My Page<")))
			.andExpect(content().string(not(containsString(">Location<"))))
			.andExpect(content().string(not(containsString(">Population<"))));
	}
}
