package com.worldmap.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.worldmap.auth.application.AdminAccessGuard;
import com.worldmap.auth.application.AuthenticatedMemberSession;
import com.worldmap.auth.application.CurrentMemberAccessService;
import com.worldmap.auth.domain.MemberRole;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(HomeController.class)
class HomeControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private AdminAccessGuard adminAccessGuard;

	@MockBean
	private CurrentMemberAccessService currentMemberAccessService;

	@Test
	void homePageRenders() throws Exception {
		given(currentMemberAccessService.currentMember(any(HttpServletRequest.class))).willReturn(Optional.empty());

		mockMvc.perform(get("/"))
			.andExpect(status().isOk())
			.andExpect(view().name("home"))
			.andExpect(model().attributeExists("modeCards"))
			.andExpect(content().string(containsString("게임 선택")))
			.andExpect(content().string(containsString("오래 버티는 게임")))
			.andExpect(content().string(containsString("짧게 푸는 게임과 추천")))
			.andExpect(content().string(containsString("바로 플레이할 게임")))
			.andExpect(content().string(containsString(">서비스 현황<")))
			.andExpect(content().string(containsString(">랭킹<")))
			.andExpect(content().string(containsString(">내 기록<")))
			.andExpect(content().string(containsString(">로그인<")))
			.andExpect(content().string(containsString(">회원가입<")))
			.andExpect(content().string(containsString("수도 퀴즈")))
			.andExpect(content().string(containsString("국기 퀴즈")))
			.andExpect(content().string(containsString("인구 비교 배틀")))
			.andExpect(content().string(containsString("국가 추천")))
			.andExpect(content().string(containsString("기록을 남기려면 로그인하세요.")))
			.andExpect(content().string(not(containsString("게임 선택하기"))))
			.andExpect(content().string(not(containsString("어울리는 나라 추천"))))
			.andExpect(content().string(not(containsString(">로그아웃<"))))
			.andExpect(content().string(not(containsString(">관리<"))))
			.andExpect(content().string(not(containsString(">Location<"))))
			.andExpect(content().string(not(containsString(">Population<"))))
			.andExpect(content().string(not(containsString("위치 미션 시작"))))
			.andExpect(content().string(not(containsString("인구수 퀴즈 시작"))))
			.andExpect(content().string(not(containsString("처음 시작 순서"))))
			.andExpect(content().string(not(containsString("기록이 이어지는 방식"))))
			.andExpect(content().string(not(containsString("오늘의 추천 플레이"))))
			.andExpect(content().string(not(containsString("Spring Boot 3 Game Platform"))))
			.andExpect(content().string(not(containsString("Current Build"))))
			.andExpect(content().string(not(containsString("ORBIT 0.4"))))
			.andExpect(content().string(not(containsString("현재 로드맵"))))
			.andExpect(content().string(not(containsString("바로 시작하는 흐름"))));
	}

	@Test
	void homePageShowsDashboardLinkForAdminSession() throws Exception {
		given(currentMemberAccessService.currentMember(any(HttpServletRequest.class))).willReturn(Optional.of(
			new AuthenticatedMemberSession(1L, "worldmap_admin", MemberRole.ADMIN)
		));
		MockHttpSession session = new MockHttpSession();

		mockMvc.perform(get("/").session(session))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString(">관리<")))
			.andExpect(content().string(containsString(">로그아웃<")))
			.andExpect(content().string(not(containsString(">회원가입<"))));
	}

	@Test
	void homePageHidesDashboardLinkWhenCurrentMemberIsMissing() throws Exception {
		given(currentMemberAccessService.currentMember(any(HttpServletRequest.class))).willReturn(Optional.empty());
		MockHttpSession session = new MockHttpSession();

		mockMvc.perform(get("/").session(session))
			.andExpect(status().isOk())
			.andExpect(content().string(not(containsString(">관리<"))));
	}
}
