package com.worldmap.demo.application;

import com.worldmap.auth.application.MemberCredentialPolicy;
import com.worldmap.auth.application.MemberPasswordHasher;
import com.worldmap.auth.domain.Member;
import com.worldmap.auth.domain.MemberRepository;
import com.worldmap.auth.domain.MemberRole;
import com.worldmap.country.domain.Country;
import com.worldmap.country.domain.CountryRepository;
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
import com.worldmap.ranking.application.LeaderboardRankingPolicy;
import com.worldmap.ranking.domain.LeaderboardGameLevel;
import com.worldmap.ranking.domain.LeaderboardGameMode;
import com.worldmap.ranking.domain.LeaderboardRecord;
import com.worldmap.ranking.domain.LeaderboardRecordRepository;
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

	private final DemoBootstrapProperties demoBootstrapProperties;
	private final MemberRepository memberRepository;
	private final MemberPasswordHasher memberPasswordHasher;
	private final MemberCredentialPolicy memberCredentialPolicy;
	private final CountryRepository countryRepository;
	private final LocationGameSessionRepository locationGameSessionRepository;
	private final LocationGameStageRepository locationGameStageRepository;
	private final LocationGameAttemptRepository locationGameAttemptRepository;
	private final PopulationGameSessionRepository populationGameSessionRepository;
	private final PopulationGameStageRepository populationGameStageRepository;
	private final PopulationGameAttemptRepository populationGameAttemptRepository;
	private final LeaderboardRecordRepository leaderboardRecordRepository;
	private final LeaderboardRankingPolicy leaderboardRankingPolicy;

	public DemoBootstrapService(
		DemoBootstrapProperties demoBootstrapProperties,
		MemberRepository memberRepository,
		MemberPasswordHasher memberPasswordHasher,
		MemberCredentialPolicy memberCredentialPolicy,
		CountryRepository countryRepository,
		LocationGameSessionRepository locationGameSessionRepository,
		LocationGameStageRepository locationGameStageRepository,
		LocationGameAttemptRepository locationGameAttemptRepository,
		PopulationGameSessionRepository populationGameSessionRepository,
		PopulationGameStageRepository populationGameStageRepository,
		PopulationGameAttemptRepository populationGameAttemptRepository,
		LeaderboardRecordRepository leaderboardRecordRepository,
		LeaderboardRankingPolicy leaderboardRankingPolicy
	) {
		this.demoBootstrapProperties = demoBootstrapProperties;
		this.memberRepository = memberRepository;
		this.memberPasswordHasher = memberPasswordHasher;
		this.memberCredentialPolicy = memberCredentialPolicy;
		this.countryRepository = countryRepository;
		this.locationGameSessionRepository = locationGameSessionRepository;
		this.locationGameStageRepository = locationGameStageRepository;
		this.locationGameAttemptRepository = locationGameAttemptRepository;
		this.populationGameSessionRepository = populationGameSessionRepository;
		this.populationGameStageRepository = populationGameStageRepository;
		this.populationGameAttemptRepository = populationGameAttemptRepository;
		this.leaderboardRecordRepository = leaderboardRecordRepository;
		this.leaderboardRankingPolicy = leaderboardRankingPolicy;
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
		provisionDemoGuestLiveSession();
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
		LocationGameSession session = LocationGameSession.ready(demoMember.getNickname(), demoMember.getId(), null, 4);
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
		PopulationGameSession session = PopulationGameSession.ready(demoMember.getNickname(), demoMember.getId(), null, 4);
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

	private void provisionDemoGuestLiveSession() {
		if (!locationGameSessionRepository.findAllByGuestSessionKeyAndMemberIdIsNull(DEMO_GUEST_SESSION_KEY).isEmpty()) {
			return;
		}

		Country mexico = country("MEX");
		LocationGameSession liveGuestSession = LocationGameSession.ready("guest_live", null, DEMO_GUEST_SESSION_KEY, 5);
		liveGuestSession.startGame(LocalDateTime.now().minusMinutes(20));
		locationGameSessionRepository.save(liveGuestSession);

		LocationGameStage pendingStage = LocationGameStage.create(liveGuestSession, 1, mexico);
		locationGameStageRepository.save(pendingStage);
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
				LeaderboardGameLevel.LEVEL_1,
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
}
