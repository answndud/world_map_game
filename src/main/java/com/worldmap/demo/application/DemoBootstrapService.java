package com.worldmap.demo.application;

import com.worldmap.auth.application.MemberCredentialPolicy;
import com.worldmap.auth.application.MemberPasswordHasher;
import com.worldmap.auth.domain.Member;
import com.worldmap.auth.domain.MemberRepository;
import com.worldmap.auth.domain.MemberRole;
import com.worldmap.country.domain.Country;
import com.worldmap.country.domain.CountryRepository;
import com.worldmap.game.flag.application.FlagQuestionCountryPoolService;
import com.worldmap.game.flag.application.FlagQuestionCountryView;
import com.worldmap.game.flag.domain.FlagGameAttempt;
import com.worldmap.game.flag.domain.FlagGameAttemptRepository;
import com.worldmap.game.flag.domain.FlagGameSession;
import com.worldmap.game.flag.domain.FlagGameSessionRepository;
import com.worldmap.game.flag.domain.FlagGameStage;
import com.worldmap.game.flag.domain.FlagGameStageRepository;
import com.worldmap.game.capital.domain.CapitalGameAttempt;
import com.worldmap.game.capital.domain.CapitalGameAttemptRepository;
import com.worldmap.game.capital.domain.CapitalGameSession;
import com.worldmap.game.capital.domain.CapitalGameSessionRepository;
import com.worldmap.game.capital.domain.CapitalGameStage;
import com.worldmap.game.capital.domain.CapitalGameStageRepository;
import com.worldmap.game.location.domain.LocationGameAttempt;
import com.worldmap.game.location.domain.LocationGameAttemptRepository;
import com.worldmap.game.location.domain.LocationGameSession;
import com.worldmap.game.location.domain.LocationGameSessionRepository;
import com.worldmap.game.location.domain.LocationGameStage;
import com.worldmap.game.location.domain.LocationGameStageRepository;
import com.worldmap.game.population.domain.PopulationGameAttempt;
import com.worldmap.game.population.domain.PopulationGameAttemptRepository;
import com.worldmap.game.population.domain.PopulationGameSession;
import com.worldmap.game.population.domain.PopulationGameSessionRepository;
import com.worldmap.game.population.domain.PopulationGameStage;
import com.worldmap.game.population.domain.PopulationGameStageRepository;
import com.worldmap.game.populationbattle.application.PopulationBattleRoundOptions;
import com.worldmap.game.populationbattle.domain.PopulationBattleGameAttempt;
import com.worldmap.game.populationbattle.domain.PopulationBattleGameAttemptRepository;
import com.worldmap.game.populationbattle.domain.PopulationBattleGameSession;
import com.worldmap.game.populationbattle.domain.PopulationBattleGameSessionRepository;
import com.worldmap.game.populationbattle.domain.PopulationBattleGameStage;
import com.worldmap.game.populationbattle.domain.PopulationBattleGameStageRepository;
import com.worldmap.ranking.application.LeaderboardRankingPolicy;
import com.worldmap.ranking.domain.LeaderboardGameMode;
import com.worldmap.ranking.domain.LeaderboardRecord;
import com.worldmap.ranking.domain.LeaderboardRecordRepository;
import com.worldmap.recommendation.application.RecommendationSurveyService;
import com.worldmap.recommendation.domain.RecommendationFeedback;
import com.worldmap.recommendation.domain.RecommendationFeedbackRepository;
import com.worldmap.recommendation.domain.RecommendationSurveyAnswers;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DemoBootstrapService {

	private static final Logger log = LoggerFactory.getLogger(DemoBootstrapService.class);
	private static final String DEMO_GUEST_SESSION_KEY = "demo-guest-live";
	private static final String DEMO_LOCATION_RUN_SIGNATURE = "demo:location:orbit_runner:1";
	private static final String DEMO_POPULATION_RUN_SIGNATURE = "demo:population:orbit_runner:1";
	private static final String DEMO_CAPITAL_RUN_SIGNATURE = "demo:capital:orbit_runner:1";
	private static final String DEMO_FLAG_RUN_SIGNATURE = "demo:flag:orbit_runner:1";
	private static final String DEMO_POPULATION_BATTLE_RUN_SIGNATURE = "demo:population-battle:orbit_runner:1";
	private static final int DEMO_CURRENT_FEEDBACK_TARGET = 5;

	private final DemoBootstrapProperties demoBootstrapProperties;
	private final MemberRepository memberRepository;
	private final MemberPasswordHasher memberPasswordHasher;
	private final MemberCredentialPolicy memberCredentialPolicy;
	private final CountryRepository countryRepository;
	private final FlagQuestionCountryPoolService flagQuestionCountryPoolService;
	private final FlagGameSessionRepository flagGameSessionRepository;
	private final FlagGameStageRepository flagGameStageRepository;
	private final FlagGameAttemptRepository flagGameAttemptRepository;
	private final CapitalGameSessionRepository capitalGameSessionRepository;
	private final CapitalGameStageRepository capitalGameStageRepository;
	private final CapitalGameAttemptRepository capitalGameAttemptRepository;
	private final LocationGameSessionRepository locationGameSessionRepository;
	private final LocationGameStageRepository locationGameStageRepository;
	private final LocationGameAttemptRepository locationGameAttemptRepository;
	private final PopulationGameSessionRepository populationGameSessionRepository;
	private final PopulationGameStageRepository populationGameStageRepository;
	private final PopulationGameAttemptRepository populationGameAttemptRepository;
	private final PopulationBattleGameSessionRepository populationBattleGameSessionRepository;
	private final PopulationBattleGameStageRepository populationBattleGameStageRepository;
	private final PopulationBattleGameAttemptRepository populationBattleGameAttemptRepository;
	private final LeaderboardRecordRepository leaderboardRecordRepository;
	private final LeaderboardRankingPolicy leaderboardRankingPolicy;
	private final RecommendationFeedbackRepository recommendationFeedbackRepository;

	public DemoBootstrapService(
		DemoBootstrapProperties demoBootstrapProperties,
		MemberRepository memberRepository,
		MemberPasswordHasher memberPasswordHasher,
		MemberCredentialPolicy memberCredentialPolicy,
		CountryRepository countryRepository,
		FlagQuestionCountryPoolService flagQuestionCountryPoolService,
		FlagGameSessionRepository flagGameSessionRepository,
		FlagGameStageRepository flagGameStageRepository,
		FlagGameAttemptRepository flagGameAttemptRepository,
		CapitalGameSessionRepository capitalGameSessionRepository,
		CapitalGameStageRepository capitalGameStageRepository,
		CapitalGameAttemptRepository capitalGameAttemptRepository,
		LocationGameSessionRepository locationGameSessionRepository,
		LocationGameStageRepository locationGameStageRepository,
		LocationGameAttemptRepository locationGameAttemptRepository,
		PopulationGameSessionRepository populationGameSessionRepository,
		PopulationGameStageRepository populationGameStageRepository,
		PopulationGameAttemptRepository populationGameAttemptRepository,
		PopulationBattleGameSessionRepository populationBattleGameSessionRepository,
		PopulationBattleGameStageRepository populationBattleGameStageRepository,
		PopulationBattleGameAttemptRepository populationBattleGameAttemptRepository,
		LeaderboardRecordRepository leaderboardRecordRepository,
		LeaderboardRankingPolicy leaderboardRankingPolicy,
		RecommendationFeedbackRepository recommendationFeedbackRepository
	) {
		this.demoBootstrapProperties = demoBootstrapProperties;
		this.memberRepository = memberRepository;
		this.memberPasswordHasher = memberPasswordHasher;
		this.memberCredentialPolicy = memberCredentialPolicy;
		this.countryRepository = countryRepository;
		this.flagQuestionCountryPoolService = flagQuestionCountryPoolService;
		this.flagGameSessionRepository = flagGameSessionRepository;
		this.flagGameStageRepository = flagGameStageRepository;
		this.flagGameAttemptRepository = flagGameAttemptRepository;
		this.capitalGameSessionRepository = capitalGameSessionRepository;
		this.capitalGameStageRepository = capitalGameStageRepository;
		this.capitalGameAttemptRepository = capitalGameAttemptRepository;
		this.locationGameSessionRepository = locationGameSessionRepository;
		this.locationGameStageRepository = locationGameStageRepository;
		this.locationGameAttemptRepository = locationGameAttemptRepository;
		this.populationGameSessionRepository = populationGameSessionRepository;
		this.populationGameStageRepository = populationGameStageRepository;
		this.populationGameAttemptRepository = populationGameAttemptRepository;
		this.populationBattleGameSessionRepository = populationBattleGameSessionRepository;
		this.populationBattleGameStageRepository = populationBattleGameStageRepository;
		this.populationBattleGameAttemptRepository = populationBattleGameAttemptRepository;
		this.leaderboardRecordRepository = leaderboardRecordRepository;
		this.leaderboardRankingPolicy = leaderboardRankingPolicy;
		this.recommendationFeedbackRepository = recommendationFeedbackRepository;
	}

	@Transactional
	public void ensureLocalDemoData() {
		if (!demoBootstrapProperties.isEnabled()) {
			return;
		}

		if (countryRepository.count() == 0) {
			log.info("Skipped demo bootstrap because country seed data is not ready yet.");
			return;
		}

		Member demoMember = provisionDemoMember();
		provisionDemoLocationRun(demoMember);
		provisionDemoPopulationRun(demoMember);
		provisionDemoCapitalRun(demoMember);
		provisionDemoFlagRun(demoMember);
		provisionDemoPopulationBattleRun(demoMember);
		provisionDemoGuestLiveSession();
		provisionCurrentRecommendationFeedbackSamples();
	}

	private Member provisionDemoMember() {
		String nickname = memberCredentialPolicy.normalizeNickname(demoBootstrapProperties.getMemberNickname());
		memberCredentialPolicy.validatePassword(demoBootstrapProperties.getMemberPassword());
		String passwordHash = memberPasswordHasher.hash(demoBootstrapProperties.getMemberPassword());

		return memberRepository.findByNicknameIgnoreCase(nickname)
			.map(existingMember -> {
				existingMember.provisionUser(passwordHash);
				return existingMember;
			})
			.orElseGet(() -> memberRepository.save(Member.create(nickname, passwordHash, MemberRole.USER)));
	}

	private void provisionDemoLocationRun(Member demoMember) {
		if (leaderboardRecordRepository.findByRunSignature(DEMO_LOCATION_RUN_SIGNATURE).isPresent()) {
			return;
		}

		Country korea = country("KOR");
		Country japan = country("JPN");
		Country germany = country("DEU");
		Country brazil = country("BRA");
		Country canada = country("CAN");

		LocalDateTime startedAt = LocalDateTime.now().minusHours(3);
		LocationGameSession session = LocationGameSession.ready(
			demoMember.getNickname(),
			demoMember.getId(),
			null,
			4
		);
		session.startGame(startedAt);
		locationGameSessionRepository.save(session);

		LocationGameStage stage1 = LocationGameStage.create(session, 1, korea);
		locationGameStageRepository.save(stage1);
		LocalDateTime stage1At = startedAt.plusMinutes(2);
		stage1.recordAttempt(true, 140, stage1At);
		locationGameAttemptRepository.save(LocationGameAttempt.create(stage1, 1, korea, true, 3, stage1At));
		session.clearCurrentStage(1, 140, stage1At);

		LocationGameStage stage2 = LocationGameStage.create(session, 2, japan);
		locationGameStageRepository.save(stage2);
		LocalDateTime stage2WrongAt = startedAt.plusMinutes(5);
		stage2.recordAttempt(false, null, stage2WrongAt);
		session.recordWrongAttempt(2, stage2WrongAt);
		locationGameAttemptRepository.save(LocationGameAttempt.create(stage2, 1, germany, false, 2, stage2WrongAt));
		LocalDateTime stage2ClearAt = startedAt.plusMinutes(7);
		stage2.recordAttempt(true, 120, stage2ClearAt);
		locationGameAttemptRepository.save(LocationGameAttempt.create(stage2, 2, japan, true, 2, stage2ClearAt));
		session.clearCurrentStage(2, 120, stage2ClearAt);

		LocationGameStage stage3 = LocationGameStage.create(session, 3, brazil);
		locationGameStageRepository.save(stage3);
		LocalDateTime stage3At = startedAt.plusMinutes(10);
		stage3.recordAttempt(true, 160, stage3At);
		locationGameAttemptRepository.save(LocationGameAttempt.create(stage3, 1, brazil, true, 2, stage3At));
		session.clearCurrentStage(3, 160, stage3At);

		LocationGameStage stage4 = LocationGameStage.create(session, 4, canada);
		locationGameStageRepository.save(stage4);
		LocalDateTime stage4WrongAt1 = startedAt.plusMinutes(13);
		stage4.recordAttempt(false, null, stage4WrongAt1);
		session.recordWrongAttempt(4, stage4WrongAt1);
		locationGameAttemptRepository.save(LocationGameAttempt.create(stage4, 1, germany, false, 1, stage4WrongAt1));
		LocalDateTime stage4WrongAt2 = startedAt.plusMinutes(15);
		stage4.recordAttempt(false, null, stage4WrongAt2);
		session.recordWrongAttempt(4, stage4WrongAt2);
		locationGameAttemptRepository.save(LocationGameAttempt.create(stage4, 2, japan, false, 0, stage4WrongAt2));
		stage4.markFailed();

		locationGameSessionRepository.save(session);
		locationGameStageRepository.saveAll(List.of(stage1, stage2, stage3, stage4));

		saveLeaderboardRecord(
			DEMO_LOCATION_RUN_SIGNATURE,
			session.getId(),
			LeaderboardGameMode.LOCATION,
			demoMember.getNickname(),
			demoMember.getId(),
			null,
			session.getTotalScore(),
			session.getClearedStageCount(),
			6,
			session.getFinishedAt()
		);
	}

	private void provisionDemoPopulationRun(Member demoMember) {
		if (leaderboardRecordRepository.findByRunSignature(DEMO_POPULATION_RUN_SIGNATURE).isPresent()) {
			return;
		}

		Country france = country("FRA");
		Country australia = country("AUS");
		Country indonesia = country("IDN");
		Country mexico = country("MEX");

		LocalDateTime startedAt = LocalDateTime.now().minusHours(2);
		PopulationGameSession session = PopulationGameSession.ready(
			demoMember.getNickname(),
			demoMember.getId(),
			null,
			4
		);
		session.startGame(startedAt);
		populationGameSessionRepository.save(session);

		PopulationGameStage stage1 = PopulationGameStage.create(
			session,
			1,
			france,
			List.of(45_000_000L, france.getPopulation(), 100_000_000L, 180_000_000L),
			2
		);
		populationGameStageRepository.save(stage1);
		LocalDateTime stage1At = startedAt.plusMinutes(3);
		stage1.recordAttempt(true, 130, stage1At);
		populationGameAttemptRepository.save(PopulationGameAttempt.create(stage1, 1, 2, france.getPopulation(), true, 3, stage1At));
		session.clearCurrentStage(1, 130, stage1At);

		PopulationGameStage stage2 = PopulationGameStage.create(
			session,
			2,
			australia,
			List.of(18_000_000L, 25_000_000L, australia.getPopulation(), 80_000_000L),
			3
		);
		populationGameStageRepository.save(stage2);
		LocalDateTime stage2WrongAt = startedAt.plusMinutes(6);
		stage2.recordAttempt(false, null, stage2WrongAt);
		session.recordWrongAttempt(2, stage2WrongAt);
		populationGameAttemptRepository.save(PopulationGameAttempt.create(stage2, 1, 2, 25_000_000L, false, 2, stage2WrongAt));
		LocalDateTime stage2ClearAt = startedAt.plusMinutes(8);
		stage2.recordAttempt(true, 110, stage2ClearAt);
		populationGameAttemptRepository.save(PopulationGameAttempt.create(stage2, 2, 3, australia.getPopulation(), true, 2, stage2ClearAt));
		session.clearCurrentStage(2, 110, stage2ClearAt);

		PopulationGameStage stage3 = PopulationGameStage.create(
			session,
			3,
			indonesia,
			List.of(120_000_000L, 180_000_000L, 220_000_000L, indonesia.getPopulation()),
			4
		);
		populationGameStageRepository.save(stage3);
		LocalDateTime stage3At = startedAt.plusMinutes(11);
		stage3.recordAttempt(true, 150, stage3At);
		populationGameAttemptRepository.save(PopulationGameAttempt.create(stage3, 1, 4, indonesia.getPopulation(), true, 2, stage3At));
		session.clearCurrentStage(3, 150, stage3At);

		PopulationGameStage stage4 = PopulationGameStage.create(
			session,
			4,
			mexico,
			List.of(60_000_000L, mexico.getPopulation(), 200_000_000L, 280_000_000L),
			2
		);
		populationGameStageRepository.save(stage4);
		LocalDateTime stage4WrongAt1 = startedAt.plusMinutes(14);
		stage4.recordAttempt(false, null, stage4WrongAt1);
		session.recordWrongAttempt(4, stage4WrongAt1);
		populationGameAttemptRepository.save(PopulationGameAttempt.create(stage4, 1, 1, 60_000_000L, false, 1, stage4WrongAt1));
		LocalDateTime stage4WrongAt2 = startedAt.plusMinutes(16);
		stage4.recordAttempt(false, null, stage4WrongAt2);
		session.recordWrongAttempt(4, stage4WrongAt2);
		populationGameAttemptRepository.save(PopulationGameAttempt.create(stage4, 2, 3, 200_000_000L, false, 0, stage4WrongAt2));
		stage4.markFailed();

		populationGameSessionRepository.save(session);
		populationGameStageRepository.saveAll(List.of(stage1, stage2, stage3, stage4));

		saveLeaderboardRecord(
			DEMO_POPULATION_RUN_SIGNATURE,
			session.getId(),
			LeaderboardGameMode.POPULATION,
			demoMember.getNickname(),
			demoMember.getId(),
			null,
			session.getTotalScore(),
			session.getClearedStageCount(),
			6,
			session.getFinishedAt()
		);
	}

	private void provisionDemoFlagRun(Member demoMember) {
		if (leaderboardRecordRepository.findByRunSignature(DEMO_FLAG_RUN_SIGNATURE).isPresent()) {
			return;
		}

		FlagQuestionCountryView japan = flagCountry("JPN");
		FlagQuestionCountryView france = flagCountry("FRA");
		FlagQuestionCountryView germany = flagCountry("DEU");
		FlagQuestionCountryView italy = flagCountry("ITA");
		FlagQuestionCountryView belgium = flagCountry("BEL");
		FlagQuestionCountryView poland = flagCountry("POL");

		LocalDateTime startedAt = LocalDateTime.now().minusMinutes(90);
		FlagGameSession session = FlagGameSession.ready(
			demoMember.getNickname(),
			demoMember.getId(),
			null,
			4
		);
		session.startGame(startedAt);
		flagGameSessionRepository.save(session);

		FlagGameStage stage1 = FlagGameStage.create(
			session,
			1,
			japan,
			List.of(japan.countryNameKr(), france.countryNameKr(), germany.countryNameKr(), italy.countryNameKr()),
			1
		);
		flagGameStageRepository.save(stage1);
		LocalDateTime stage1At = startedAt.plusMinutes(2);
		stage1.recordAttempt(true, 150, stage1At);
		flagGameAttemptRepository.save(
			FlagGameAttempt.create(stage1, 1, 1, stage1.getOptions().get(0), true, 3, stage1At)
		);
		session.clearCurrentStage(1, 150, stage1At);

		FlagGameStage stage2 = FlagGameStage.create(
			session,
			2,
			france,
			List.of(belgium.countryNameKr(), france.countryNameKr(), poland.countryNameKr(), italy.countryNameKr()),
			2
		);
		flagGameStageRepository.save(stage2);
		LocalDateTime stage2WrongAt = startedAt.plusMinutes(5);
		stage2.recordAttempt(false, null, stage2WrongAt);
		session.recordWrongAttempt(2, stage2WrongAt);
		flagGameAttemptRepository.save(
			FlagGameAttempt.create(stage2, 1, 1, stage2.getOptions().get(0), false, 2, stage2WrongAt)
		);
		LocalDateTime stage2ClearAt = startedAt.plusMinutes(7);
		stage2.recordAttempt(true, 135, stage2ClearAt);
		flagGameAttemptRepository.save(
			FlagGameAttempt.create(stage2, 2, 2, stage2.getOptions().get(1), true, 2, stage2ClearAt)
		);
		session.clearCurrentStage(2, 135, stage2ClearAt);

		FlagGameStage stage3 = FlagGameStage.create(
			session,
			3,
			germany,
			List.of(germany.countryNameKr(), japan.countryNameKr(), france.countryNameKr(), poland.countryNameKr()),
			1
		);
		flagGameStageRepository.save(stage3);
		LocalDateTime stage3WrongAt1 = startedAt.plusMinutes(10);
		stage3.recordAttempt(false, null, stage3WrongAt1);
		session.recordWrongAttempt(3, stage3WrongAt1);
		flagGameAttemptRepository.save(
			FlagGameAttempt.create(stage3, 1, 2, stage3.getOptions().get(1), false, 1, stage3WrongAt1)
		);
		LocalDateTime stage3WrongAt2 = startedAt.plusMinutes(12);
		stage3.recordAttempt(false, null, stage3WrongAt2);
		session.recordWrongAttempt(3, stage3WrongAt2);
		flagGameAttemptRepository.save(
			FlagGameAttempt.create(stage3, 2, 3, stage3.getOptions().get(2), false, 0, stage3WrongAt2)
		);
		stage3.markFailed();

		flagGameSessionRepository.save(session);
		flagGameStageRepository.saveAll(List.of(stage1, stage2, stage3));

		saveLeaderboardRecord(
			DEMO_FLAG_RUN_SIGNATURE,
			session.getId(),
			LeaderboardGameMode.FLAG,
			demoMember.getNickname(),
			demoMember.getId(),
			null,
			session.getTotalScore(),
			session.getClearedStageCount(),
			4,
			session.getFinishedAt()
		);
	}

	private void provisionDemoCapitalRun(Member demoMember) {
		if (leaderboardRecordRepository.findByRunSignature(DEMO_CAPITAL_RUN_SIGNATURE).isPresent()) {
			return;
		}

		Country korea = country("KOR");
		Country japan = country("JPN");
		Country france = country("FRA");
		Country italy = country("ITA");
		Country germany = country("DEU");
		Country spain = country("ESP");
		Country portugal = country("PRT");

		LocalDateTime startedAt = LocalDateTime.now().minusMinutes(80);
		CapitalGameSession session = CapitalGameSession.ready(
			demoMember.getNickname(),
			demoMember.getId(),
			null,
			4
		);
		session.startGame(startedAt);
		capitalGameSessionRepository.save(session);

		CapitalGameStage stage1 = CapitalGameStage.create(
			session,
			1,
			korea,
			List.of(korea.getCapitalCityKr(), japan.getCapitalCityKr(), france.getCapitalCityKr(), italy.getCapitalCityKr()),
			1
		);
		capitalGameStageRepository.save(stage1);
		LocalDateTime stage1At = startedAt.plusMinutes(2);
		stage1.recordAttempt(true, 150, stage1At);
		capitalGameAttemptRepository.save(
			CapitalGameAttempt.create(stage1, 1, 1, stage1.getOptions().get(0), true, 3, stage1At)
		);
		session.clearCurrentStage(1, 150, stage1At);

		CapitalGameStage stage2 = CapitalGameStage.create(
			session,
			2,
			france,
			List.of(germany.getCapitalCityKr(), france.getCapitalCityKr(), spain.getCapitalCityKr(), portugal.getCapitalCityKr()),
			2
		);
		capitalGameStageRepository.save(stage2);
		LocalDateTime stage2WrongAt = startedAt.plusMinutes(5);
		stage2.recordAttempt(false, null, stage2WrongAt);
		session.recordWrongAttempt(2, stage2WrongAt);
		capitalGameAttemptRepository.save(
			CapitalGameAttempt.create(stage2, 1, 1, stage2.getOptions().get(0), false, 2, stage2WrongAt)
		);
		LocalDateTime stage2ClearAt = startedAt.plusMinutes(7);
		stage2.recordAttempt(true, 135, stage2ClearAt);
		capitalGameAttemptRepository.save(
			CapitalGameAttempt.create(stage2, 2, 2, stage2.getOptions().get(1), true, 2, stage2ClearAt)
		);
		session.clearCurrentStage(2, 135, stage2ClearAt);

		CapitalGameStage stage3 = CapitalGameStage.create(
			session,
			3,
			japan,
			List.of(japan.getCapitalCityKr(), korea.getCapitalCityKr(), italy.getCapitalCityKr(), germany.getCapitalCityKr()),
			1
		);
		capitalGameStageRepository.save(stage3);
		LocalDateTime stage3WrongAt1 = startedAt.plusMinutes(10);
		stage3.recordAttempt(false, null, stage3WrongAt1);
		session.recordWrongAttempt(3, stage3WrongAt1);
		capitalGameAttemptRepository.save(
			CapitalGameAttempt.create(stage3, 1, 2, stage3.getOptions().get(1), false, 1, stage3WrongAt1)
		);
		LocalDateTime stage3WrongAt2 = startedAt.plusMinutes(12);
		stage3.recordAttempt(false, null, stage3WrongAt2);
		session.recordWrongAttempt(3, stage3WrongAt2);
		capitalGameAttemptRepository.save(
			CapitalGameAttempt.create(stage3, 2, 3, stage3.getOptions().get(2), false, 0, stage3WrongAt2)
		);
		stage3.markFailed();

		capitalGameSessionRepository.save(session);
		capitalGameStageRepository.saveAll(List.of(stage1, stage2, stage3));

		saveLeaderboardRecord(
			DEMO_CAPITAL_RUN_SIGNATURE,
			session.getId(),
			LeaderboardGameMode.CAPITAL,
			demoMember.getNickname(),
			demoMember.getId(),
			null,
			session.getTotalScore(),
			session.getClearedStageCount(),
			4,
			session.getFinishedAt()
		);
	}

	private void provisionDemoPopulationBattleRun(Member demoMember) {
		if (leaderboardRecordRepository.findByRunSignature(DEMO_POPULATION_BATTLE_RUN_SIGNATURE).isPresent()) {
			return;
		}

		Country usa = country("USA");
		Country mexico = country("MEX");
		Country spain = country("ESP");
		Country brazil = country("BRA");
		Country india = country("IND");
		Country canada = country("CAN");

		LocalDateTime startedAt = LocalDateTime.now().minusMinutes(70);
		PopulationBattleGameSession session = PopulationBattleGameSession.ready(
			demoMember.getNickname(),
			demoMember.getId(),
			null,
			4
		);
		session.startGame(startedAt);
		populationBattleGameSessionRepository.save(session);

		PopulationBattleGameStage stage1 = PopulationBattleGameStage.create(
			session,
			1,
			"더 많은 인구를 가진 나라를 고르세요.",
			new PopulationBattleRoundOptions(usa, mexico, 1),
			1
		);
		populationBattleGameStageRepository.save(stage1);
		LocalDateTime stage1At = startedAt.plusMinutes(2);
		stage1.recordAttempt(true, 145, stage1At);
		populationBattleGameAttemptRepository.save(
			PopulationBattleGameAttempt.create(stage1, 1, 1, stage1.optionName(1), true, 3, stage1At)
		);
		session.clearCurrentStage(1, 145, stage1At);

		PopulationBattleGameStage stage2 = PopulationBattleGameStage.create(
			session,
			2,
			"더 많은 인구를 가진 나라를 고르세요.",
			new PopulationBattleRoundOptions(spain, brazil, 2),
			2
		);
		populationBattleGameStageRepository.save(stage2);
		LocalDateTime stage2WrongAt = startedAt.plusMinutes(5);
		stage2.recordAttempt(false, null, stage2WrongAt);
		session.recordWrongAttempt(2, stage2WrongAt);
		populationBattleGameAttemptRepository.save(
			PopulationBattleGameAttempt.create(stage2, 1, 1, stage2.optionName(1), false, 2, stage2WrongAt)
		);
		LocalDateTime stage2ClearAt = startedAt.plusMinutes(7);
		stage2.recordAttempt(true, 130, stage2ClearAt);
		populationBattleGameAttemptRepository.save(
			PopulationBattleGameAttempt.create(stage2, 2, 2, stage2.optionName(2), true, 2, stage2ClearAt)
		);
		session.clearCurrentStage(2, 130, stage2ClearAt);

		PopulationBattleGameStage stage3 = PopulationBattleGameStage.create(
			session,
			3,
			"더 많은 인구를 가진 나라를 고르세요.",
			new PopulationBattleRoundOptions(india, canada, 1),
			1
		);
		populationBattleGameStageRepository.save(stage3);
		LocalDateTime stage3WrongAt1 = startedAt.plusMinutes(10);
		stage3.recordAttempt(false, null, stage3WrongAt1);
		session.recordWrongAttempt(3, stage3WrongAt1);
		populationBattleGameAttemptRepository.save(
			PopulationBattleGameAttempt.create(stage3, 1, 2, stage3.optionName(2), false, 1, stage3WrongAt1)
		);
		LocalDateTime stage3WrongAt2 = startedAt.plusMinutes(12);
		stage3.recordAttempt(false, null, stage3WrongAt2);
		session.recordWrongAttempt(3, stage3WrongAt2);
		populationBattleGameAttemptRepository.save(
			PopulationBattleGameAttempt.create(stage3, 2, 2, stage3.optionName(2), false, 0, stage3WrongAt2)
		);
		stage3.markFailed();

		populationBattleGameSessionRepository.save(session);
		populationBattleGameStageRepository.saveAll(List.of(stage1, stage2, stage3));

		saveLeaderboardRecord(
			DEMO_POPULATION_BATTLE_RUN_SIGNATURE,
			session.getId(),
			LeaderboardGameMode.POPULATION_BATTLE,
			demoMember.getNickname(),
			demoMember.getId(),
			null,
			session.getTotalScore(),
			session.getClearedStageCount(),
			4,
			session.getFinishedAt()
		);
	}

	private void provisionDemoGuestLiveSession() {
		if (!locationGameSessionRepository.findAllByGuestSessionKeyAndMemberIdIsNull(DEMO_GUEST_SESSION_KEY).isEmpty()) {
			return;
		}

		Country mexico = country("MEX");
		LocationGameSession liveGuestSession = LocationGameSession.ready(
			"guest_live",
			null,
			DEMO_GUEST_SESSION_KEY,
			5
		);
		liveGuestSession.startGame(LocalDateTime.now().minusMinutes(20));
		locationGameSessionRepository.save(liveGuestSession);

		LocationGameStage pendingStage = LocationGameStage.create(liveGuestSession, 1, mexico);
		locationGameStageRepository.save(pendingStage);
	}

	private void provisionCurrentRecommendationFeedbackSamples() {
		long currentFeedbackCount = recommendationFeedbackRepository.countBySurveyVersionAndEngineVersion(
			RecommendationSurveyService.SURVEY_VERSION,
			RecommendationSurveyService.ENGINE_VERSION
		);

		if (currentFeedbackCount >= DEMO_CURRENT_FEEDBACK_TARGET) {
			return;
		}

		List<RecommendationFeedback> demoFeedbacks = List.of(
			RecommendationFeedback.create(
				RecommendationSurveyService.SURVEY_VERSION,
				RecommendationSurveyService.ENGINE_VERSION,
				5,
				new RecommendationSurveyAnswers(
					RecommendationSurveyAnswers.ClimatePreference.MILD,
					RecommendationSurveyAnswers.SeasonStylePreference.DISTINCT,
					RecommendationSurveyAnswers.SeasonTolerance.LOW,
					RecommendationSurveyAnswers.PacePreference.BALANCED,
					RecommendationSurveyAnswers.CrowdPreference.BALANCED,
					RecommendationSurveyAnswers.CostQualityPreference.QUALITY_FIRST,
					RecommendationSurveyAnswers.HousingPreference.BALANCED,
					RecommendationSurveyAnswers.EnvironmentPreference.MIXED,
					RecommendationSurveyAnswers.MobilityPreference.BALANCED,
					RecommendationSurveyAnswers.EnglishSupportNeed.HIGH,
					RecommendationSurveyAnswers.NewcomerSupportNeed.HIGH,
					RecommendationSurveyAnswers.ImportanceLevel.HIGH,
					RecommendationSurveyAnswers.ImportanceLevel.HIGH,
					RecommendationSurveyAnswers.ImportanceLevel.MEDIUM,
					RecommendationSurveyAnswers.ImportanceLevel.MEDIUM,
					RecommendationSurveyAnswers.ImportanceLevel.MEDIUM,
					RecommendationSurveyAnswers.ImportanceLevel.HIGH,
					RecommendationSurveyAnswers.WorkLifePreference.BALANCED,
					RecommendationSurveyAnswers.SettlementPreference.STABILITY,
					RecommendationSurveyAnswers.FutureBasePreference.STABLE_BASE
				)
			),
			RecommendationFeedback.create(
				RecommendationSurveyService.SURVEY_VERSION,
				RecommendationSurveyService.ENGINE_VERSION,
				4,
				new RecommendationSurveyAnswers(
					RecommendationSurveyAnswers.ClimatePreference.WARM,
					RecommendationSurveyAnswers.SeasonStylePreference.STABLE,
					RecommendationSurveyAnswers.SeasonTolerance.MEDIUM,
					RecommendationSurveyAnswers.PacePreference.FAST,
					RecommendationSurveyAnswers.CrowdPreference.BALANCED,
					RecommendationSurveyAnswers.CostQualityPreference.BALANCED,
					RecommendationSurveyAnswers.HousingPreference.CENTER_FIRST,
					RecommendationSurveyAnswers.EnvironmentPreference.CITY,
					RecommendationSurveyAnswers.MobilityPreference.TRANSIT_FIRST,
					RecommendationSurveyAnswers.EnglishSupportNeed.HIGH,
					RecommendationSurveyAnswers.NewcomerSupportNeed.MEDIUM,
					RecommendationSurveyAnswers.ImportanceLevel.MEDIUM,
					RecommendationSurveyAnswers.ImportanceLevel.MEDIUM,
					RecommendationSurveyAnswers.ImportanceLevel.HIGH,
					RecommendationSurveyAnswers.ImportanceLevel.MEDIUM,
					RecommendationSurveyAnswers.ImportanceLevel.HIGH,
					RecommendationSurveyAnswers.ImportanceLevel.HIGH,
					RecommendationSurveyAnswers.WorkLifePreference.DRIVE_FIRST,
					RecommendationSurveyAnswers.SettlementPreference.EXPERIENCE,
					RecommendationSurveyAnswers.FutureBasePreference.BALANCED
				)
			),
			RecommendationFeedback.create(
				RecommendationSurveyService.SURVEY_VERSION,
				RecommendationSurveyService.ENGINE_VERSION,
				4,
				new RecommendationSurveyAnswers(
					RecommendationSurveyAnswers.ClimatePreference.MILD,
					RecommendationSurveyAnswers.SeasonStylePreference.STABLE,
					RecommendationSurveyAnswers.SeasonTolerance.MEDIUM,
					RecommendationSurveyAnswers.PacePreference.RELAXED,
					RecommendationSurveyAnswers.CrowdPreference.CALM,
					RecommendationSurveyAnswers.CostQualityPreference.VALUE_FIRST,
					RecommendationSurveyAnswers.HousingPreference.SPACE_FIRST,
					RecommendationSurveyAnswers.EnvironmentPreference.NATURE,
					RecommendationSurveyAnswers.MobilityPreference.SPACE_FIRST,
					RecommendationSurveyAnswers.EnglishSupportNeed.MEDIUM,
					RecommendationSurveyAnswers.NewcomerSupportNeed.MEDIUM,
					RecommendationSurveyAnswers.ImportanceLevel.HIGH,
					RecommendationSurveyAnswers.ImportanceLevel.MEDIUM,
					RecommendationSurveyAnswers.ImportanceLevel.MEDIUM,
					RecommendationSurveyAnswers.ImportanceLevel.MEDIUM,
					RecommendationSurveyAnswers.ImportanceLevel.LOW,
					RecommendationSurveyAnswers.ImportanceLevel.MEDIUM,
					RecommendationSurveyAnswers.WorkLifePreference.LIFE_FIRST,
					RecommendationSurveyAnswers.SettlementPreference.BALANCED,
					RecommendationSurveyAnswers.FutureBasePreference.BALANCED
				)
			),
			RecommendationFeedback.create(
				RecommendationSurveyService.SURVEY_VERSION,
				RecommendationSurveyService.ENGINE_VERSION,
				5,
				new RecommendationSurveyAnswers(
					RecommendationSurveyAnswers.ClimatePreference.WARM,
					RecommendationSurveyAnswers.SeasonStylePreference.STABLE,
					RecommendationSurveyAnswers.SeasonTolerance.HIGH,
					RecommendationSurveyAnswers.PacePreference.BALANCED,
					RecommendationSurveyAnswers.CrowdPreference.BALANCED,
					RecommendationSurveyAnswers.CostQualityPreference.VALUE_FIRST,
					RecommendationSurveyAnswers.HousingPreference.BALANCED,
					RecommendationSurveyAnswers.EnvironmentPreference.MIXED,
					RecommendationSurveyAnswers.MobilityPreference.TRANSIT_FIRST,
					RecommendationSurveyAnswers.EnglishSupportNeed.MEDIUM,
					RecommendationSurveyAnswers.NewcomerSupportNeed.HIGH,
					RecommendationSurveyAnswers.ImportanceLevel.MEDIUM,
					RecommendationSurveyAnswers.ImportanceLevel.MEDIUM,
					RecommendationSurveyAnswers.ImportanceLevel.HIGH,
					RecommendationSurveyAnswers.ImportanceLevel.HIGH,
					RecommendationSurveyAnswers.ImportanceLevel.HIGH,
					RecommendationSurveyAnswers.ImportanceLevel.MEDIUM,
					RecommendationSurveyAnswers.WorkLifePreference.BALANCED,
					RecommendationSurveyAnswers.SettlementPreference.EXPERIENCE,
					RecommendationSurveyAnswers.FutureBasePreference.LIGHT_START
				)
			),
			RecommendationFeedback.create(
				RecommendationSurveyService.SURVEY_VERSION,
				RecommendationSurveyService.ENGINE_VERSION,
				4,
				new RecommendationSurveyAnswers(
					RecommendationSurveyAnswers.ClimatePreference.MILD,
					RecommendationSurveyAnswers.SeasonStylePreference.DISTINCT,
					RecommendationSurveyAnswers.SeasonTolerance.MEDIUM,
					RecommendationSurveyAnswers.PacePreference.BALANCED,
					RecommendationSurveyAnswers.CrowdPreference.BALANCED,
					RecommendationSurveyAnswers.CostQualityPreference.BALANCED,
					RecommendationSurveyAnswers.HousingPreference.BALANCED,
					RecommendationSurveyAnswers.EnvironmentPreference.MIXED,
					RecommendationSurveyAnswers.MobilityPreference.BALANCED,
					RecommendationSurveyAnswers.EnglishSupportNeed.MEDIUM,
					RecommendationSurveyAnswers.NewcomerSupportNeed.HIGH,
					RecommendationSurveyAnswers.ImportanceLevel.HIGH,
					RecommendationSurveyAnswers.ImportanceLevel.HIGH,
					RecommendationSurveyAnswers.ImportanceLevel.MEDIUM,
					RecommendationSurveyAnswers.ImportanceLevel.MEDIUM,
					RecommendationSurveyAnswers.ImportanceLevel.MEDIUM,
					RecommendationSurveyAnswers.ImportanceLevel.MEDIUM,
					RecommendationSurveyAnswers.WorkLifePreference.BALANCED,
					RecommendationSurveyAnswers.SettlementPreference.STABILITY,
					RecommendationSurveyAnswers.FutureBasePreference.STABLE_BASE
				)
			)
		);

		int missingFeedbackCount = DEMO_CURRENT_FEEDBACK_TARGET - (int) currentFeedbackCount;
		recommendationFeedbackRepository.saveAll(demoFeedbacks.stream().limit(missingFeedbackCount).toList());
	}

	private void saveLeaderboardRecord(
		String runSignature,
		UUID sessionId,
		LeaderboardGameMode gameMode,
		String playerNickname,
		Long memberId,
		String guestSessionKey,
		int totalScore,
		int clearedStageCount,
		int totalAttemptCount,
		LocalDateTime finishedAt
	) {
		long rankingScore = leaderboardRankingPolicy.rankingScore(totalScore, clearedStageCount, totalAttemptCount);
		leaderboardRecordRepository.save(
			LeaderboardRecord.create(
				runSignature,
				sessionId,
				gameMode,
				playerNickname,
				memberId,
				guestSessionKey,
				totalScore,
				rankingScore,
				clearedStageCount,
				totalAttemptCount,
				finishedAt
			)
		);
	}

	private Country country(String iso3Code) {
		return countryRepository.findByIso3CodeIgnoreCase(iso3Code)
			.orElseThrow(() -> new IllegalStateException("데모 bootstrap용 국가가 없습니다: " + iso3Code));
	}

	private FlagQuestionCountryView flagCountry(String iso3Code) {
		return flagQuestionCountryPoolService.findAvailableCountry(iso3Code)
			.orElseThrow(() -> new IllegalStateException("데모 bootstrap용 국기 자산 국가가 없습니다: " + iso3Code));
	}
}
