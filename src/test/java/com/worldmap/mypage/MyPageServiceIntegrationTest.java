package com.worldmap.mypage;

import static org.assertj.core.api.Assertions.assertThat;

import com.worldmap.auth.domain.Member;
import com.worldmap.auth.domain.MemberRepository;
import com.worldmap.auth.domain.MemberRole;
import com.worldmap.country.domain.CountryRepository;
import com.worldmap.game.capital.application.CapitalGameService;
import com.worldmap.game.capital.domain.CapitalGameStage;
import com.worldmap.game.capital.domain.CapitalGameStageRepository;
import com.worldmap.game.flag.application.FlagGameService;
import com.worldmap.game.flag.domain.FlagGameStage;
import com.worldmap.game.flag.domain.FlagGameStageRepository;
import com.worldmap.game.common.application.GameSessionAccessContext;
import com.worldmap.game.location.application.LocationGameService;
import com.worldmap.game.location.domain.LocationGameStage;
import com.worldmap.game.location.domain.LocationGameStageRepository;
import com.worldmap.game.population.application.PopulationGameService;
import com.worldmap.game.population.domain.PopulationGameStage;
import com.worldmap.game.population.domain.PopulationGameStageRepository;
import com.worldmap.game.populationbattle.application.PopulationBattleGameService;
import com.worldmap.game.populationbattle.domain.PopulationBattleGameStage;
import com.worldmap.game.populationbattle.domain.PopulationBattleGameStageRepository;
import com.worldmap.mypage.application.MyPageModePerformanceView;
import com.worldmap.mypage.application.MyPageDashboardView;
import com.worldmap.mypage.application.MyPageService;
import com.worldmap.ranking.domain.LeaderboardRecordRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class MyPageServiceIntegrationTest {

	@Autowired
	private MyPageService myPageService;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private CountryRepository countryRepository;

	@Autowired
	private LocationGameService locationGameService;

	@Autowired
	private LocationGameStageRepository locationGameStageRepository;

	@Autowired
	private PopulationGameService populationGameService;

	@Autowired
	private PopulationGameStageRepository populationGameStageRepository;

	@Autowired
	private CapitalGameService capitalGameService;

	@Autowired
	private CapitalGameStageRepository capitalGameStageRepository;

	@Autowired
	private FlagGameService flagGameService;

	@Autowired
	private FlagGameStageRepository flagGameStageRepository;

	@Autowired
	private PopulationBattleGameService populationBattleGameService;

	@Autowired
	private PopulationBattleGameStageRepository populationBattleGameStageRepository;

	@Autowired
	private LeaderboardRecordRepository leaderboardRecordRepository;

	@BeforeEach
	void setUp() {
		leaderboardRecordRepository.deleteAll();
		memberRepository.deleteAll();
	}

	@Test
	void loadDashboardIncludesRawStagePerformanceByMode() {
		Member member = memberRepository.save(Member.create("stats_runner", "hash", MemberRole.USER));
		GameSessionAccessContext accessContext = GameSessionAccessContext.forMember(member.getId());

		UUID locationSessionId = locationGameService.startMemberGame(member.getId(), member.getNickname()).sessionId();
		LocationGameStage locationStageOne = locationGameStageRepository.findBySessionIdAndStageNumber(locationSessionId, 1)
			.orElseThrow();
		String wrongLocationIso3Code = countryRepository.findAll().stream()
			.map(country -> country.getIso3Code())
			.filter(iso3Code -> !iso3Code.equals(locationStageOne.getTargetCountryIso3Code()))
			.findFirst()
			.orElseThrow();

		locationGameService.submitAnswer(locationSessionId, 1, wrongLocationIso3Code, accessContext);
		locationGameService.submitAnswer(locationSessionId, 1, locationStageOne.getTargetCountryIso3Code(), accessContext);

		LocationGameStage locationStageTwo = locationGameStageRepository.findBySessionIdAndStageNumber(locationSessionId, 2)
			.orElseThrow();
		locationGameService.submitAnswer(locationSessionId, 2, locationStageTwo.getTargetCountryIso3Code(), accessContext);

		LocationGameStage locationStageThree = locationGameStageRepository.findBySessionIdAndStageNumber(locationSessionId, 3)
			.orElseThrow();
		String secondWrongLocationIso3Code = countryRepository.findAll().stream()
			.map(country -> country.getIso3Code())
			.filter(iso3Code -> !iso3Code.equals(locationStageThree.getTargetCountryIso3Code()))
			.findFirst()
			.orElseThrow();
		locationGameService.submitAnswer(locationSessionId, 3, secondWrongLocationIso3Code, accessContext);
		locationGameService.submitAnswer(locationSessionId, 3, secondWrongLocationIso3Code, accessContext);

		UUID populationSessionId = populationGameService.startMemberGame(member.getId(), member.getNickname()).sessionId();
		PopulationGameStage populationStageOne = populationGameStageRepository.findBySessionIdAndStageNumber(populationSessionId, 1)
			.orElseThrow();
		populationGameService.submitAnswer(populationSessionId, 1, populationStageOne.getCorrectOptionNumber(), accessContext);

		PopulationGameStage populationStageTwo = populationGameStageRepository.findBySessionIdAndStageNumber(populationSessionId, 2)
			.orElseThrow();
		int wrongPopulationOption = populationStageTwo.getCorrectOptionNumber() == 1 ? 2 : 1;
		populationGameService.submitAnswer(populationSessionId, 2, wrongPopulationOption, accessContext);
		populationGameService.submitAnswer(populationSessionId, 2, wrongPopulationOption, accessContext);
		populationGameService.submitAnswer(populationSessionId, 2, wrongPopulationOption, accessContext);

		MyPageDashboardView dashboard = myPageService.loadDashboard(member.getId());
		MyPageModePerformanceView locationPerformance = performanceFor(dashboard, "국가 위치 찾기");
		MyPageModePerformanceView populationPerformance = performanceFor(dashboard, "인구수 퀴즈");

		assertThat(dashboard.totalCompletedRuns()).isEqualTo(2);
		assertThat(locationPerformance).isNotNull();
		assertThat(locationPerformance.completedRunCount()).isEqualTo(1);
		assertThat(locationPerformance.clearedStageCount()).isEqualTo(2);
		assertThat(locationPerformance.firstTryClearRateLabel()).isEqualTo("50%");
		assertThat(locationPerformance.averageAttemptsPerClearLabel()).isEqualTo("1.5회");

		assertThat(populationPerformance).isNotNull();
		assertThat(populationPerformance.completedRunCount()).isEqualTo(1);
		assertThat(populationPerformance.clearedStageCount()).isEqualTo(1);
		assertThat(populationPerformance.firstTryClearRateLabel()).isEqualTo("100%");
		assertThat(populationPerformance.averageAttemptsPerClearLabel()).isEqualTo("1회");
	}

	@Test
	void loadDashboardIncludesAllFiveGameModes() {
		Member member = memberRepository.save(Member.create("five_mode_runner", "hash", MemberRole.USER));
		GameSessionAccessContext accessContext = GameSessionAccessContext.forMember(member.getId());

		finishLocationGameWithGameOver(member, accessContext);
		finishPopulationGameWithGameOver(member, accessContext);
		finishCapitalGameWithGameOver(member, accessContext);
		finishFlagGameWithGameOver(member, accessContext);
		finishPopulationBattleGameWithGameOver(member, accessContext);

		MyPageDashboardView dashboard = myPageService.loadDashboard(member.getId());

		assertThat(dashboard.totalCompletedRuns()).isEqualTo(5);
		assertThat(dashboard.bestRuns())
			.extracting(bestRun -> bestRun.gameModeLabel())
			.containsExactly(
				"국가 위치 찾기",
				"수도 퀴즈",
				"국기 퀴즈",
				"인구 비교 배틀",
				"인구수 퀴즈"
			);
		assertThat(dashboard.modePerformances())
			.extracting(MyPageModePerformanceView::gameModeLabel)
			.containsExactly(
				"국가 위치 찾기",
				"수도 퀴즈",
				"국기 퀴즈",
				"인구 비교 배틀",
				"인구수 퀴즈"
			);
		assertThat(dashboard.recentPlays())
			.extracting(play -> play.gameModeLabel())
			.contains(
				"국가 위치 찾기",
				"수도 퀴즈",
				"국기 퀴즈",
				"인구 비교 배틀",
				"인구수 퀴즈"
			);
		assertThat(dashboard.recentPlays())
			.allSatisfy(play -> assertThat(play.currentRank()).isNotNull());
	}

	private MyPageModePerformanceView performanceFor(MyPageDashboardView dashboard, String label) {
		return dashboard.modePerformances().stream()
			.filter(performance -> performance.gameModeLabel().equals(label))
			.findFirst()
			.orElseThrow();
	}

	private void finishLocationGameWithGameOver(Member member, GameSessionAccessContext accessContext) {
		UUID sessionId = locationGameService.startMemberGame(member.getId(), member.getNickname()).sessionId();
		LocationGameStage stage = locationGameStageRepository.findBySessionIdAndStageNumber(sessionId, 1).orElseThrow();
		String wrongIso3Code = countryRepository.findAll().stream()
			.map(country -> country.getIso3Code())
			.filter(iso3Code -> !iso3Code.equals(stage.getTargetCountryIso3Code()))
			.findFirst()
			.orElseThrow();

		locationGameService.submitAnswer(sessionId, 1, wrongIso3Code, accessContext);
		locationGameService.submitAnswer(sessionId, 1, wrongIso3Code, accessContext);
		locationGameService.submitAnswer(sessionId, 1, wrongIso3Code, accessContext);
	}

	private void finishPopulationGameWithGameOver(Member member, GameSessionAccessContext accessContext) {
		UUID sessionId = populationGameService.startMemberGame(member.getId(), member.getNickname()).sessionId();
		PopulationGameStage stage = populationGameStageRepository.findBySessionIdAndStageNumber(sessionId, 1).orElseThrow();
		int wrongOption = stage.getCorrectOptionNumber() == 1 ? 2 : 1;

		populationGameService.submitAnswer(sessionId, 1, wrongOption, accessContext);
		populationGameService.submitAnswer(sessionId, 1, wrongOption, accessContext);
		populationGameService.submitAnswer(sessionId, 1, wrongOption, accessContext);
	}

	private void finishCapitalGameWithGameOver(Member member, GameSessionAccessContext accessContext) {
		UUID sessionId = capitalGameService.startMemberGame(member.getId(), member.getNickname()).sessionId();
		CapitalGameStage stage = capitalGameStageRepository.findBySessionIdAndStageNumber(sessionId, 1).orElseThrow();
		int wrongOption = stage.getCorrectOptionNumber() == 1 ? 2 : 1;

		capitalGameService.submitAnswer(sessionId, 1, wrongOption, accessContext);
		capitalGameService.submitAnswer(sessionId, 1, wrongOption, accessContext);
		capitalGameService.submitAnswer(sessionId, 1, wrongOption, accessContext);
	}

	private void finishFlagGameWithGameOver(Member member, GameSessionAccessContext accessContext) {
		UUID sessionId = flagGameService.startMemberGame(member.getId(), member.getNickname()).sessionId();
		FlagGameStage stage = flagGameStageRepository.findBySessionIdAndStageNumber(sessionId, 1).orElseThrow();
		int wrongOption = stage.getCorrectOptionNumber() == 1 ? 2 : 1;

		flagGameService.submitAnswer(sessionId, 1, wrongOption, accessContext);
		flagGameService.submitAnswer(sessionId, 1, wrongOption, accessContext);
		flagGameService.submitAnswer(sessionId, 1, wrongOption, accessContext);
	}

	private void finishPopulationBattleGameWithGameOver(Member member, GameSessionAccessContext accessContext) {
		UUID sessionId = populationBattleGameService.startMemberGame(member.getId(), member.getNickname()).sessionId();
		PopulationBattleGameStage stage = populationBattleGameStageRepository.findBySessionIdAndStageNumber(sessionId, 1)
			.orElseThrow();
		int wrongOption = stage.getCorrectOptionNumber() == 1 ? 2 : 1;

		populationBattleGameService.submitAnswer(sessionId, 1, wrongOption, accessContext);
		populationBattleGameService.submitAnswer(sessionId, 1, wrongOption, accessContext);
		populationBattleGameService.submitAnswer(sessionId, 1, wrongOption, accessContext);
	}
}
