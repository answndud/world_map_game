package com.worldmap.stats;

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
import com.worldmap.ranking.application.LeaderboardEntryView;
import com.worldmap.ranking.application.LeaderboardService;
import com.worldmap.ranking.application.LeaderboardView;
import com.worldmap.ranking.domain.LeaderboardGameMode;
import com.worldmap.ranking.domain.LeaderboardScope;
import com.worldmap.stats.application.ServiceActivityService;
import com.worldmap.stats.application.ServiceActivityView;
import com.worldmap.stats.web.StatsPageController;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(StatsPageController.class)
class StatsPageControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ServiceActivityService serviceActivityService;

	@MockBean
	private LeaderboardService leaderboardService;

	@MockBean
	private AdminAccessGuard adminAccessGuard;

	@MockBean
	private CurrentMemberAccessService currentMemberAccessService;

	@Test
	void statsPageRendersPublicMetricsWithoutDashboardLinkForGuest() throws Exception {
		given(currentMemberAccessService.currentMember(any(HttpServletRequest.class))).willReturn(Optional.empty());
		given(serviceActivityService.loadTodayActivity()).willReturn(
			new ServiceActivityView(12, 3, 4, 9, 5, 3, 1, 0, 2, 2)
		);
		given(leaderboardService.getLeaderboard(LeaderboardGameMode.LOCATION, LeaderboardScope.DAILY, 3)).willReturn(
			new LeaderboardView(
				LeaderboardGameMode.LOCATION,
				LeaderboardScope.DAILY,
				LocalDate.now(),
				List.of(new LeaderboardEntryView(1, "orbit_runner", 420, 3, 6, LocalDateTime.now()))
			)
		);
		given(leaderboardService.getLeaderboard(LeaderboardGameMode.CAPITAL, LeaderboardScope.DAILY, 3)).willReturn(
			new LeaderboardView(
				LeaderboardGameMode.CAPITAL,
				LeaderboardScope.DAILY,
				LocalDate.now(),
				List.of(new LeaderboardEntryView(1, "capital_hunter", 410, 3, 5, LocalDateTime.now()))
			)
		);
		given(leaderboardService.getLeaderboard(LeaderboardGameMode.FLAG, LeaderboardScope.DAILY, 3)).willReturn(
			new LeaderboardView(
				LeaderboardGameMode.FLAG,
				LeaderboardScope.DAILY,
				LocalDate.now(),
				List.of(new LeaderboardEntryView(1, "flag_runner", 415, 3, 5, LocalDateTime.now()))
			)
		);
		given(leaderboardService.getLeaderboard(LeaderboardGameMode.POPULATION, LeaderboardScope.DAILY, 3)).willReturn(
			new LeaderboardView(
				LeaderboardGameMode.POPULATION,
				LeaderboardScope.DAILY,
				LocalDate.now(),
				List.of(new LeaderboardEntryView(1, "guest_live", 390, 3, 5, LocalDateTime.now()))
			)
		);
		given(leaderboardService.getLeaderboard(LeaderboardGameMode.POPULATION_BATTLE, LeaderboardScope.DAILY, 3)).willReturn(
			new LeaderboardView(
				LeaderboardGameMode.POPULATION_BATTLE,
				LeaderboardScope.DAILY,
				LocalDate.now(),
				List.of(new LeaderboardEntryView(1, "battle_runner", 405, 4, 6, LocalDateTime.now()))
			)
		);
		mockMvc.perform(get("/stats"))
			.andExpect(status().isOk())
			.andExpect(view().name("stats/index"))
			.andExpect(model().attributeExists("activity"))
			.andExpect(content().string(containsString("서비스 현황")))
			.andExpect(content().string(containsString(">서비스 현황<")))
			.andExpect(content().string(not(containsString(">관리<"))))
			.andExpect(content().string(containsString("오늘 플레이어")))
			.andExpect(content().string(containsString("오늘 많이 끝난 게임")))
			.andExpect(content().string(containsString("오래 버티는 게임 Top 3")))
			.andExpect(content().string(containsString("짧게 푸는 게임 Top 3")))
			.andExpect(content().string(containsString("수도 퀴즈")))
			.andExpect(content().string(containsString("국기 퀴즈")))
			.andExpect(content().string(containsString("인구 비교 배틀")))
			.andExpect(content().string(containsString("battle_runner")))
			.andExpect(content().string(containsString("capital_hunter")))
			.andExpect(content().string(containsString("flag_runner")))
			.andExpect(content().string(containsString("orbit_runner")))
			.andExpect(content().string(not(containsString("Level 2 하이라이트"))));
	}

	@Test
	void statsPageShowsDashboardLinkForAdminSession() throws Exception {
		given(currentMemberAccessService.currentMember(any(HttpServletRequest.class))).willReturn(Optional.of(
			new AuthenticatedMemberSession(1L, "worldmap_admin", MemberRole.ADMIN)
		));
		MockHttpSession session = new MockHttpSession();
		given(serviceActivityService.loadTodayActivity()).willReturn(
			new ServiceActivityView(12, 3, 4, 9, 5, 3, 1, 0, 2, 2)
		);
		given(leaderboardService.getLeaderboard(LeaderboardGameMode.LOCATION, LeaderboardScope.DAILY, 3)).willReturn(
			new LeaderboardView(LeaderboardGameMode.LOCATION, LeaderboardScope.DAILY, LocalDate.now(), List.of())
		);
		given(leaderboardService.getLeaderboard(LeaderboardGameMode.CAPITAL, LeaderboardScope.DAILY, 3)).willReturn(
			new LeaderboardView(LeaderboardGameMode.CAPITAL, LeaderboardScope.DAILY, LocalDate.now(), List.of())
		);
		given(leaderboardService.getLeaderboard(LeaderboardGameMode.FLAG, LeaderboardScope.DAILY, 3)).willReturn(
			new LeaderboardView(LeaderboardGameMode.FLAG, LeaderboardScope.DAILY, LocalDate.now(), List.of())
		);
		given(leaderboardService.getLeaderboard(LeaderboardGameMode.POPULATION, LeaderboardScope.DAILY, 3)).willReturn(
			new LeaderboardView(LeaderboardGameMode.POPULATION, LeaderboardScope.DAILY, LocalDate.now(), List.of())
		);
		given(leaderboardService.getLeaderboard(LeaderboardGameMode.POPULATION_BATTLE, LeaderboardScope.DAILY, 3)).willReturn(
			new LeaderboardView(LeaderboardGameMode.POPULATION_BATTLE, LeaderboardScope.DAILY, LocalDate.now(), List.of())
		);

		mockMvc.perform(get("/stats").session(session))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString(">관리<")));
	}
}
