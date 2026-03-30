package com.worldmap.ranking;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.worldmap.auth.application.AdminAccessGuard;
import com.worldmap.auth.application.CurrentMemberAccessService;
import com.worldmap.ranking.application.LeaderboardEntryView;
import com.worldmap.ranking.application.LeaderboardService;
import com.worldmap.ranking.application.LeaderboardView;
import com.worldmap.ranking.domain.LeaderboardGameMode;
import com.worldmap.ranking.domain.LeaderboardScope;
import com.worldmap.ranking.web.LeaderboardPageController;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(LeaderboardPageController.class)
class LeaderboardPageControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private LeaderboardService leaderboardService;

	@MockBean
	private AdminAccessGuard adminAccessGuard;

	@MockBean
	private CurrentMemberAccessService currentMemberAccessService;

	@Test
	void rankingPageLoadsOnlyInitialActiveBoardFromService() throws Exception {
		given(currentMemberAccessService.currentMember(any())).willReturn(Optional.empty());
		given(leaderboardService.getLeaderboard(LeaderboardGameMode.LOCATION, LeaderboardScope.ALL, 10)).willReturn(
			new LeaderboardView(
				LeaderboardGameMode.LOCATION,
				LeaderboardScope.ALL,
				LocalDate.now(),
				List.of(new LeaderboardEntryView(1, "orbit_runner", 420, 5, 7, LocalDateTime.now()))
			)
		);

		mockMvc.perform(get("/ranking"))
			.andExpect(status().isOk())
			.andExpect(view().name("ranking/index"))
			.andExpect(model().attributeExists("locationAll"))
			.andExpect(model().attributeDoesNotExist(
				"capitalAll",
				"flagAll",
				"populationAll",
				"populationBattleAll",
				"locationDaily",
				"capitalDaily",
				"flagDaily",
				"populationDaily",
				"populationBattleDaily"
			))
			.andExpect(content().string(containsString("orbit_runner")))
			.andExpect(content().string(containsString("이 보드를 열면 최신 랭킹을 불러옵니다.")));

		verify(leaderboardService).getLeaderboard(LeaderboardGameMode.LOCATION, LeaderboardScope.ALL, 10);
		verifyNoMoreInteractions(leaderboardService);
	}
}
