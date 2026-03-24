package com.worldmap.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.worldmap.auth.domain.Member;
import com.worldmap.auth.domain.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthFlowIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private MemberRepository memberRepository;

	@BeforeEach
	void clearMembers() {
		memberRepository.deleteAll();
	}

	@Test
	void signupCreatesSimpleAccountAndKeepsMemberSession() throws Exception {
		MockHttpSession browserSession = new MockHttpSession();

		mockMvc.perform(
			post("/signup")
				.session(browserSession)
				.param("nickname", "orbit_runner")
				.param("password", "secret1234")
		)
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/mypage"));

		Member member = memberRepository.findByNicknameIgnoreCase("orbit_runner").orElseThrow();
		assertThat(member.getPasswordHash()).isNotEqualTo("secret1234");
		assertThat(member.getLastLoginAt()).isNotNull();

		mockMvc.perform(get("/mypage").session(browserSession))
			.andExpect(status().isOk())
			.andExpect(view().name("mypage"))
			.andExpect(content().string(containsString("orbit_runner")))
			.andExpect(content().string(containsString("로그아웃")));
	}

	@Test
	void loginFailureStaysOnLoginPageWithErrorMessage() throws Exception {
		memberRepository.save(Member.create("orbit_runner", "$2a$10$e0NRzHX0tM5f0Y4b9Kz6uOrUrs9jwELyhl725LLJoPLD114F8CbnW", com.worldmap.auth.domain.MemberRole.USER));

		mockMvc.perform(
			post("/login")
				.param("nickname", "orbit_runner")
				.param("password", "wrong-pass")
		)
			.andExpect(status().isOk())
			.andExpect(view().name("auth/login"))
			.andExpect(content().string(containsString("닉네임 또는 비밀번호가 올바르지 않습니다.")));
	}
}
