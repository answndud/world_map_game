package com.worldmap.web;

import static com.worldmap.auth.application.MemberSessionManager.MEMBER_ID_ATTRIBUTE;
import static com.worldmap.auth.application.MemberSessionManager.MEMBER_NICKNAME_ATTRIBUTE;
import static com.worldmap.auth.application.MemberSessionManager.MEMBER_ROLE_ATTRIBUTE;
import static com.worldmap.auth.domain.MemberRole.USER;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.worldmap.mypage.application.MyPageBestRunView;
import com.worldmap.mypage.application.MyPageDashboardView;
import com.worldmap.mypage.application.MyPageModePerformanceView;
import com.worldmap.mypage.application.MyPageRecentPlayView;
import com.worldmap.mypage.application.MyPageService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MyPageController.class)
@Import(com.worldmap.auth.application.MemberSessionManager.class)
class MyPageControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private MyPageService myPageService;

	@Test
	void myPageShowsGuestPromptWhenNotLoggedIn() throws Exception {
		mockMvc.perform(get("/mypage"))
			.andExpect(status().isOk())
			.andExpect(view().name("mypage"))
			.andExpect(content().string(containsString("기록을 남기려면 로그인")))
			.andExpect(content().string(containsString("회원가입")))
			.andExpect(content().string(containsString(">My Page<")))
			.andExpect(content().string(not(containsString(">Dashboard<"))))
			.andExpect(content().string(not(containsString(">Location<"))))
			.andExpect(content().string(not(containsString(">Population<"))));
	}

	@Test
	void myPageShowsConnectedMemberStateWhenLoggedIn() throws Exception {
		MockHttpSession session = new MockHttpSession();
		session.setAttribute(MEMBER_ID_ATTRIBUTE, 1L);
		session.setAttribute(MEMBER_NICKNAME_ATTRIBUTE, "orbit_runner");
		session.setAttribute(MEMBER_ROLE_ATTRIBUTE, USER.name());
		given(myPageService.loadDashboard(eq(1L))).willReturn(
			new MyPageDashboardView(
				"orbit_runner",
				3,
				List.of(
					new MyPageBestRunView("국가 위치 찾기", 2L, 440, 1, 4),
					new MyPageBestRunView("수도 맞히기", 1L, 390, 2, 3),
					new MyPageBestRunView("국기 보고 나라 맞히기", 1L, 360, 3, 2)
				),
				List.of(
					new MyPageModePerformanceView("국가 위치 찾기", 2, 5, "60%", "1.4회"),
					new MyPageModePerformanceView("국가 인구수 맞추기", 1, 3, "100%", "1회")
				),
				List.of(
					new MyPageRecentPlayView(
						"국가 위치 찾기",
						440,
						4,
						5,
						1,
						LocalDateTime.of(2026, 3, 24, 18, 30),
						"orbit_runner"
					)
				)
			)
		);

		mockMvc.perform(get("/mypage").session(session))
			.andExpect(status().isOk())
			.andExpect(view().name("mypage"))
			.andExpect(content().string(containsString("내 기록 허브")))
			.andExpect(content().string(containsString("orbit_runner")))
			.andExpect(content().string(containsString("440점 / 현재 #1")))
			.andExpect(content().string(containsString("수도 맞히기")))
			.andExpect(content().string(containsString("플레이 성향")))
			.andExpect(content().string(not(containsString("Level 2 하이라이트"))))
			.andExpect(content().string(containsString("1트 클리어율")))
			.andExpect(content().string(containsString("1.4회")))
			.andExpect(content().string(containsString("최근 플레이")))
			.andExpect(content().string(containsString("로그아웃")));
	}
}
