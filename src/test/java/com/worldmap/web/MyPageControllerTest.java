package com.worldmap.web;

import static com.worldmap.auth.application.MemberSessionManager.MEMBER_ID_ATTRIBUTE;
import static com.worldmap.auth.application.MemberSessionManager.MEMBER_NICKNAME_ATTRIBUTE;
import static com.worldmap.auth.application.MemberSessionManager.MEMBER_ROLE_ATTRIBUTE;
import static com.worldmap.auth.domain.MemberRole.USER;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MyPageController.class)
@Import(com.worldmap.auth.application.MemberSessionManager.class)
class MyPageControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void myPageShowsGuestPromptWhenNotLoggedIn() throws Exception {
		mockMvc.perform(get("/mypage"))
			.andExpect(status().isOk())
			.andExpect(view().name("mypage"))
			.andExpect(content().string(containsString("기록을 남기려면 로그인")))
			.andExpect(content().string(containsString("회원가입")))
			.andExpect(content().string(containsString(">My Page<")))
			.andExpect(content().string(not(containsString(">Location<"))))
			.andExpect(content().string(not(containsString(">Population<"))));
	}

	@Test
	void myPageShowsConnectedMemberStateWhenLoggedIn() throws Exception {
		MockHttpSession session = new MockHttpSession();
		session.setAttribute(MEMBER_ID_ATTRIBUTE, 1L);
		session.setAttribute(MEMBER_NICKNAME_ATTRIBUTE, "orbit_runner");
		session.setAttribute(MEMBER_ROLE_ATTRIBUTE, USER.name());

		mockMvc.perform(get("/mypage").session(session))
			.andExpect(status().isOk())
			.andExpect(view().name("mypage"))
			.andExpect(content().string(containsString("내 기록 허브")))
			.andExpect(content().string(containsString("orbit_runner")))
			.andExpect(content().string(containsString("로그아웃")));
	}
}
