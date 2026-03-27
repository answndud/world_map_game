package com.worldmap.demo;

import static org.assertj.core.api.Assertions.assertThat;

import com.worldmap.auth.application.MemberPasswordHasher;
import com.worldmap.auth.domain.Member;
import com.worldmap.auth.domain.MemberRepository;
import com.worldmap.auth.domain.MemberRole;
import com.worldmap.admin.application.AdminRecommendationOpsReviewService;
import com.worldmap.admin.application.AdminRecommendationOpsReviewView;
import com.worldmap.game.capital.domain.CapitalGameSessionRepository;
import com.worldmap.game.location.domain.LocationGameSessionRepository;
import com.worldmap.game.population.domain.PopulationGameSessionRepository;
import com.worldmap.game.populationbattle.domain.PopulationBattleGameSessionRepository;
import com.worldmap.ranking.domain.LeaderboardRecordRepository;
import com.worldmap.recommendation.application.RecommendationSurveyService;
import com.worldmap.recommendation.domain.RecommendationFeedbackRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
	properties = {
		"worldmap.admin.bootstrap.enabled=true",
		"worldmap.admin.bootstrap.nickname=worldmap_admin",
		"worldmap.admin.bootstrap.password=secret123",
		"worldmap.demo.bootstrap.enabled=true",
		"worldmap.demo.bootstrap.member-nickname=orbit_runner",
		"worldmap.demo.bootstrap.member-password=secret123"
	}
)
@ActiveProfiles({"test", "local"})
class DemoBootstrapIntegrationTest {

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private MemberPasswordHasher memberPasswordHasher;

	@Autowired
	private LeaderboardRecordRepository leaderboardRecordRepository;

	@Autowired
	private LocationGameSessionRepository locationGameSessionRepository;

	@Autowired
	private PopulationGameSessionRepository populationGameSessionRepository;

	@Autowired
	private CapitalGameSessionRepository capitalGameSessionRepository;

	@Autowired
	private PopulationBattleGameSessionRepository populationBattleGameSessionRepository;

	@Autowired
	private RecommendationFeedbackRepository recommendationFeedbackRepository;

	@Autowired
	private AdminRecommendationOpsReviewService adminRecommendationOpsReviewService;

	@Test
	void startupBootstrapCreatesLocalAdminUserAndSampleRuns() {
		Member adminMember = memberRepository.findByNicknameIgnoreCase("worldmap_admin").orElseThrow();
		Member demoMember = memberRepository.findByNicknameIgnoreCase("orbit_runner").orElseThrow();

		assertThat(adminMember.getRole()).isEqualTo(MemberRole.ADMIN);
		assertThat(demoMember.getRole()).isEqualTo(MemberRole.USER);
		assertThat(memberPasswordHasher.matches("secret123", adminMember.getPasswordHash())).isTrue();
		assertThat(memberPasswordHasher.matches("secret123", demoMember.getPasswordHash())).isTrue();

		assertThat(leaderboardRecordRepository.findByRunSignature("demo:location:orbit_runner:1")).isPresent();
		assertThat(leaderboardRecordRepository.findByRunSignature("demo:population:orbit_runner:1")).isPresent();
		assertThat(leaderboardRecordRepository.findByRunSignature("demo:capital:orbit_runner:1")).isPresent();
		assertThat(leaderboardRecordRepository.findByRunSignature("demo:flag:orbit_runner:1")).isPresent();
		assertThat(leaderboardRecordRepository.findByRunSignature("demo:population-battle:orbit_runner:1")).isPresent();
		assertThat(locationGameSessionRepository.findAllByGuestSessionKeyAndMemberIdIsNull("demo-guest-live")).hasSize(1);
		assertThat(populationGameSessionRepository.countByMemberIdAndFinishedAtIsNotNull(demoMember.getId())).isGreaterThanOrEqualTo(1L);
		assertThat(capitalGameSessionRepository.countByMemberIdAndFinishedAtIsNotNull(demoMember.getId())).isGreaterThanOrEqualTo(1L);
		assertThat(populationBattleGameSessionRepository.countByMemberIdAndFinishedAtIsNotNull(demoMember.getId())).isGreaterThanOrEqualTo(1L);
		assertThat(
			recommendationFeedbackRepository.countBySurveyVersionAndEngineVersion(
				RecommendationSurveyService.SURVEY_VERSION,
				RecommendationSurveyService.ENGINE_VERSION
			)
		).isGreaterThanOrEqualTo(5L);

		AdminRecommendationOpsReviewView opsReview = adminRecommendationOpsReviewService.loadReview();
		assertThat(opsReview.priorityActionTitle()).isEqualTo("현재 엔진 유지");
		assertThat(opsReview.currentVersionResponseCount()).isGreaterThanOrEqualTo(5);
		assertThat(opsReview.priorityScenarioIds()).isEmpty();
	}
}
