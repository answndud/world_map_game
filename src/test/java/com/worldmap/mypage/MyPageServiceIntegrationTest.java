package com.worldmap.mypage;

import static org.assertj.core.api.Assertions.assertThat;

import com.worldmap.auth.domain.Member;
import com.worldmap.auth.domain.MemberRepository;
import com.worldmap.auth.domain.MemberRole;
import com.worldmap.country.domain.CountryRepository;
import com.worldmap.game.location.application.LocationGameService;
import com.worldmap.game.location.domain.LocationGameLevel;
import com.worldmap.game.location.domain.LocationGameStage;
import com.worldmap.game.location.domain.LocationGameStageRepository;
import com.worldmap.game.population.application.PopulationGameService;
import com.worldmap.game.population.domain.PopulationGameLevel;
import com.worldmap.game.population.domain.PopulationGameStage;
import com.worldmap.game.population.domain.PopulationGameStageRepository;
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
	private LeaderboardRecordRepository leaderboardRecordRepository;

	@BeforeEach
	void setUp() {
		leaderboardRecordRepository.deleteAll();
		memberRepository.deleteAll();
	}

	@Test
	void loadDashboardIncludesRawStagePerformanceByMode() {
		Member member = memberRepository.save(Member.create("stats_runner", "hash", MemberRole.USER));

		UUID locationSessionId = locationGameService.startMemberGame(member.getId(), member.getNickname()).sessionId();
		LocationGameStage locationStageOne = locationGameStageRepository.findBySessionIdAndStageNumber(locationSessionId, 1)
			.orElseThrow();
		String wrongLocationIso3Code = countryRepository.findAll().stream()
			.map(country -> country.getIso3Code())
			.filter(iso3Code -> !iso3Code.equals(locationStageOne.getTargetCountryIso3Code()))
			.findFirst()
			.orElseThrow();

		locationGameService.submitAnswer(locationSessionId, 1, wrongLocationIso3Code);
		locationGameService.submitAnswer(locationSessionId, 1, locationStageOne.getTargetCountryIso3Code());

		LocationGameStage locationStageTwo = locationGameStageRepository.findBySessionIdAndStageNumber(locationSessionId, 2)
			.orElseThrow();
		locationGameService.submitAnswer(locationSessionId, 2, locationStageTwo.getTargetCountryIso3Code());

		LocationGameStage locationStageThree = locationGameStageRepository.findBySessionIdAndStageNumber(locationSessionId, 3)
			.orElseThrow();
		String secondWrongLocationIso3Code = countryRepository.findAll().stream()
			.map(country -> country.getIso3Code())
			.filter(iso3Code -> !iso3Code.equals(locationStageThree.getTargetCountryIso3Code()))
			.findFirst()
			.orElseThrow();
		locationGameService.submitAnswer(locationSessionId, 3, secondWrongLocationIso3Code);
		locationGameService.submitAnswer(locationSessionId, 3, secondWrongLocationIso3Code);

		UUID populationSessionId = populationGameService.startMemberGame(member.getId(), member.getNickname()).sessionId();
		PopulationGameStage populationStageOne = populationGameStageRepository.findBySessionIdAndStageNumber(populationSessionId, 1)
			.orElseThrow();
		populationGameService.submitAnswer(populationSessionId, 1, populationStageOne.getCorrectOptionNumber());

		PopulationGameStage populationStageTwo = populationGameStageRepository.findBySessionIdAndStageNumber(populationSessionId, 2)
			.orElseThrow();
		int wrongPopulationOption = populationStageTwo.getCorrectOptionNumber() == 1 ? 2 : 1;
		populationGameService.submitAnswer(populationSessionId, 2, wrongPopulationOption);
		populationGameService.submitAnswer(populationSessionId, 2, wrongPopulationOption);
		populationGameService.submitAnswer(populationSessionId, 2, wrongPopulationOption);

		MyPageDashboardView dashboard = myPageService.loadDashboard(member.getId());

		assertThat(dashboard.totalCompletedRuns()).isEqualTo(2);
		assertThat(dashboard.locationPerformance()).isNotNull();
		assertThat(dashboard.locationPerformance().completedRunCount()).isEqualTo(1);
		assertThat(dashboard.locationPerformance().clearedStageCount()).isEqualTo(2);
		assertThat(dashboard.locationPerformance().firstTryClearRateLabel()).isEqualTo("50%");
		assertThat(dashboard.locationPerformance().averageAttemptsPerClearLabel()).isEqualTo("1.5회");

		assertThat(dashboard.populationPerformance()).isNotNull();
		assertThat(dashboard.populationPerformance().completedRunCount()).isEqualTo(1);
		assertThat(dashboard.populationPerformance().clearedStageCount()).isEqualTo(1);
		assertThat(dashboard.populationPerformance().firstTryClearRateLabel()).isEqualTo("100%");
		assertThat(dashboard.populationPerformance().averageAttemptsPerClearLabel()).isEqualTo("1회");
	}

	@Test
	void loadDashboardIncludesLevelTwoBestHighlights() {
		Member member = memberRepository.save(Member.create("level_two_runner", "hash", MemberRole.USER));

		UUID locationSessionId = locationGameService
			.startMemberGame(member.getId(), member.getNickname(), LocationGameLevel.LEVEL_2)
			.sessionId();
		LocationGameStage locationStageOne = locationGameStageRepository.findBySessionIdAndStageNumber(locationSessionId, 1)
			.orElseThrow();
		String wrongLocationIso3Code = countryRepository.findAll().stream()
			.map(country -> country.getIso3Code())
			.filter(iso3Code -> !iso3Code.equals(locationStageOne.getTargetCountryIso3Code()))
			.findFirst()
			.orElseThrow();

		locationGameService.submitAnswer(locationSessionId, 1, wrongLocationIso3Code);
		locationGameService.submitAnswer(locationSessionId, 1, locationStageOne.getTargetCountryIso3Code());

		LocationGameStage locationStageTwo = locationGameStageRepository.findBySessionIdAndStageNumber(locationSessionId, 2)
			.orElseThrow();
		String locationStageTwoWrongIso3Code = countryRepository.findAll().stream()
			.map(country -> country.getIso3Code())
			.filter(iso3Code -> !iso3Code.equals(locationStageTwo.getTargetCountryIso3Code()))
			.findFirst()
			.orElseThrow();
		for (int attempt = 0; attempt < 2; attempt++) {
			locationGameService.submitAnswer(locationSessionId, 2, locationStageTwoWrongIso3Code);
		}

		UUID populationSessionId = populationGameService
			.startMemberGame(member.getId(), member.getNickname(), PopulationGameLevel.LEVEL_2)
			.sessionId();
		PopulationGameStage populationStageOne = populationGameStageRepository.findBySessionIdAndStageNumber(populationSessionId, 1)
			.orElseThrow();
		populationGameService.submitAnswer(populationSessionId, 1, null, populationStageOne.getTargetPopulation());

		for (int attempt = 0; attempt < 3; attempt++) {
			populationGameService.submitAnswer(populationSessionId, 2, null, 1L);
		}

		MyPageDashboardView dashboard = myPageService.loadDashboard(member.getId());

		assertThat(dashboard.locationLevel2Best()).isNotNull();
		assertThat(dashboard.locationLevel2Best().gameModeLabel()).isEqualTo("국가 위치 찾기 · Level 2");
		assertThat(dashboard.locationLevel2Best().completedRunCount()).isEqualTo(1);
		assertThat(dashboard.locationLevel2Best().bestRank()).isEqualTo(1);
		assertThat(dashboard.locationLevel2Best().totalScore()).isEqualTo(115);

		assertThat(dashboard.populationLevel2Best()).isNotNull();
		assertThat(dashboard.populationLevel2Best().gameModeLabel()).isEqualTo("국가 인구수 맞추기 · Level 2");
		assertThat(dashboard.populationLevel2Best().completedRunCount()).isEqualTo(1);
		assertThat(dashboard.populationLevel2Best().bestRank()).isEqualTo(1);
		assertThat(dashboard.populationLevel2Best().totalScore()).isGreaterThan(0);
	}
}
