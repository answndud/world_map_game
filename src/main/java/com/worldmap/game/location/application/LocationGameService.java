package com.worldmap.game.location.application;

import com.worldmap.common.exception.ResourceNotFoundException;
import com.worldmap.country.domain.Country;
import com.worldmap.country.domain.CountryRepository;
import com.worldmap.game.common.application.GameSessionAccessContext;
import com.worldmap.game.common.domain.GameSessionStatus;
import com.worldmap.game.location.domain.LocationGameAttempt;
import com.worldmap.game.location.domain.LocationGameAttemptRepository;
import com.worldmap.game.location.domain.LocationGameStage;
import com.worldmap.game.location.domain.LocationGameStageRepository;
import com.worldmap.game.location.domain.LocationGameSession;
import com.worldmap.game.location.domain.LocationGameSessionRepository;
import com.worldmap.game.location.domain.LocationGameStageStatus;
import com.worldmap.ranking.application.LeaderboardService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LocationGameService {

	private static final int MINIMUM_COUNTRY_COUNT = 3;
	private static final int LEVEL_ONE_COUNTRY_LIMIT = 72;

	private final CountryRepository countryRepository;
	private final LocationGameSessionRepository locationGameSessionRepository;
	private final LocationGameStageRepository locationGameStageRepository;
	private final LocationGameAttemptRepository locationGameAttemptRepository;
	private final LocationGameDifficultyPolicy locationGameDifficultyPolicy;
	private final LocationGameScoringPolicy locationGameScoringPolicy;
	private final LeaderboardService leaderboardService;

	public LocationGameService(
		CountryRepository countryRepository,
		LocationGameSessionRepository locationGameSessionRepository,
		LocationGameStageRepository locationGameStageRepository,
		LocationGameAttemptRepository locationGameAttemptRepository,
		LocationGameDifficultyPolicy locationGameDifficultyPolicy,
		LocationGameScoringPolicy locationGameScoringPolicy,
		LeaderboardService leaderboardService
	) {
		this.countryRepository = countryRepository;
		this.locationGameSessionRepository = locationGameSessionRepository;
		this.locationGameStageRepository = locationGameStageRepository;
		this.locationGameAttemptRepository = locationGameAttemptRepository;
		this.locationGameDifficultyPolicy = locationGameDifficultyPolicy;
		this.locationGameScoringPolicy = locationGameScoringPolicy;
		this.leaderboardService = leaderboardService;
	}

	@Transactional
	public LocationGameStartView startGuestGame(String nickname, String guestSessionKey) {
		return startGame(normalizeNickname(nickname), null, guestSessionKey);
	}

	@Transactional
	public LocationGameStartView startMemberGame(Long memberId, String memberNickname) {
		return startGame(memberNickname, memberId, null);
	}

	private LocationGameStartView startGame(
		String playerNickname,
		Long memberId,
		String guestSessionKey
	) {
		List<Country> countries = getCountriesSortedByPopulation();

		if (countries.size() < MINIMUM_COUNTRY_COUNT) {
			throw new IllegalStateException("위치 찾기 게임을 시작하기 위한 국가 데이터가 충분하지 않습니다.");
		}

		LocationGameSession session = LocationGameSession.ready(playerNickname, memberId, guestSessionKey, 1);
		session = locationGameSessionRepository.save(session);
		createNextStage(session, 1, countries, List.of());
		session.startGame(LocalDateTime.now());

		return new LocationGameStartView(
			session.getId(),
			session.getPlayerNickname(),
			session.getStatus(),
			session.getTotalRounds(),
			session.getLivesRemaining(),
			"/games/location/play/" + session.getId()
		);
	}

	@Transactional(readOnly = true)
	public LocationGameStateView getCurrentState(UUID sessionId, GameSessionAccessContext accessContext) {
		LocationGameSession session = getSession(sessionId, accessContext);
		if (session.getStatus() != GameSessionStatus.IN_PROGRESS) {
			throw new IllegalStateException("이미 종료된 게임입니다.");
		}

		LocationGameStage stage = getStage(sessionId, session.getCurrentStageNumber());
		LocationGameDifficultyPlan difficultyPlan = resolveDifficulty(stage.getStageNumber());

		return new LocationGameStateView(
			session.getId(),
			stage.getStageNumber(),
			difficultyPlan.label(),
			session.getClearedStageCount(),
			session.getTotalScore(),
			session.getLivesRemaining(),
			stage.getTargetCountryName(),
			session.getStatus()
		);
	}

	@Transactional
	public LocationGameStartView restartGame(UUID sessionId, GameSessionAccessContext accessContext) {
		LocationGameSession session = getSession(sessionId, accessContext);

		if (session.getStatus() != GameSessionStatus.GAME_OVER && session.getStatus() != GameSessionStatus.FINISHED) {
			throw new IllegalStateException("종료된 게임만 다시 시작할 수 있습니다.");
		}

		List<Country> countries = getCountriesSortedByPopulation();
		if (countries.size() < MINIMUM_COUNTRY_COUNT) {
			throw new IllegalStateException("위치 찾기 게임을 시작하기 위한 국가 데이터가 충분하지 않습니다.");
		}

		locationGameAttemptRepository.deleteAllByStageSessionId(sessionId);
		locationGameAttemptRepository.flush();
		locationGameStageRepository.deleteAllBySessionId(sessionId);
		locationGameStageRepository.flush();

		session.restart(1);
		createNextStage(session, 1, countries, List.of());
		session.startGame(LocalDateTime.now());

		return new LocationGameStartView(
			session.getId(),
			session.getPlayerNickname(),
			session.getStatus(),
			session.getTotalRounds(),
			session.getLivesRemaining(),
			"/games/location/play/" + session.getId()
		);
	}

	@Transactional
	public LocationGameAnswerView submitAnswer(
		UUID sessionId,
		Integer stageNumber,
		String selectedCountryIso3Code,
		GameSessionAccessContext accessContext
	) {
		LocationGameSession session = getSession(sessionId, accessContext);

		if (session.getStatus() != GameSessionStatus.IN_PROGRESS) {
			throw new IllegalStateException("진행 중인 게임만 답안을 제출할 수 있습니다.");
		}

		if (!session.getCurrentStageNumber().equals(stageNumber)) {
			throw new IllegalStateException("현재 진행 중인 라운드와 일치하지 않습니다.");
		}

		LocationGameStage stage = getStage(sessionId, stageNumber);
		Country selectedCountry = countryRepository.findByIso3CodeIgnoreCase(normalizeCountryCode(selectedCountryIso3Code))
			.orElseThrow(() -> new IllegalArgumentException("지원하지 않는 국가입니다: " + selectedCountryIso3Code));
		int attemptNumber = stage.nextAttemptNumber();
		LocalDateTime attemptedAt = LocalDateTime.now();
		LocationAnswerJudgement judgement = locationGameScoringPolicy.judge(
			selectedCountry.getIso3Code(),
			stage.getTargetCountryIso3Code(),
			stageNumber,
			attemptNumber,
			session.getLivesRemaining()
		);

		stage.recordAttempt(
			judgement.correct(),
			judgement.awardedScore(),
			attemptedAt
		);

		if (judgement.correct()) {
			List<Country> countries = getCountriesSortedByPopulation();
			List<LocationGameStage> existingStages = locationGameStageRepository.findAllBySessionIdOrderByStageNumber(sessionId);
			createNextStage(session, stageNumber + 1, countries, existingStages);
			session.clearCurrentStage(stageNumber, judgement.awardedScore(), attemptedAt);
		} else {
			session.recordWrongAttempt(stageNumber, attemptedAt);

			if (session.getStatus() == GameSessionStatus.GAME_OVER) {
				stage.markFailed();
			}
		}

		locationGameAttemptRepository.save(
			LocationGameAttempt.create(
				stage,
				attemptNumber,
				selectedCountry,
				judgement.correct(),
				session.getLivesRemaining(),
				attemptedAt
			)
		);

		if (session.getStatus() != GameSessionStatus.IN_PROGRESS) {
			leaderboardService.recordLocationResult(
				session,
				Math.toIntExact(locationGameAttemptRepository.countByStageSessionId(sessionId))
			);
		}

		LocationGameAnswerOutcome outcome = determineOutcome(judgement.correct(), session.getStatus());
		LocationGameDifficultyPlan nextDifficultyPlan = session.getStatus() == GameSessionStatus.IN_PROGRESS
			? resolveDifficulty(session.getCurrentStageNumber())
			: null;

		return new LocationGameAnswerView(
			session.getId(),
			stage.getStageNumber(),
			stage.getTargetCountryName(),
			selectedCountry.getNameKr(),
			selectedCountry.getIso3Code(),
			judgement.correct(),
			judgement.awardedScore(),
			session.getTotalScore(),
			session.getClearedStageCount(),
			session.getLivesRemaining(),
			session.getStatus() == GameSessionStatus.IN_PROGRESS ? session.getCurrentStageNumber() : null,
			nextDifficultyPlan != null ? nextDifficultyPlan.label() : null,
			session.getStatus(),
			outcome,
			"/games/location/result/" + session.getId()
		);
	}

	@Transactional(readOnly = true)
	public LocationGameSessionResultView getSessionResult(UUID sessionId, GameSessionAccessContext accessContext) {
		LocationGameSession session = getSession(sessionId, accessContext);
		assertResultAccessible(session);
		Map<Long, Country> countriesById = new LinkedHashMap<>();
		Map<String, Country> countriesByIso3Code = new LinkedHashMap<>();
		countryRepository.findAll().forEach(country -> {
			countriesById.put(country.getId(), country);
			countriesByIso3Code.put(country.getIso3Code(), country);
		});
		Map<Long, List<LocationGameAttemptResultView>> attemptsByStageId = new LinkedHashMap<>();

		locationGameAttemptRepository.findAllByStageSessionIdOrderByStageStageNumberAscAttemptNumberAsc(sessionId)
			.forEach(attempt -> attemptsByStageId.computeIfAbsent(attempt.getStage().getId(), ignored -> new ArrayList<>())
				.add(toAttemptResultView(session, attempt, countriesById, countriesByIso3Code)));

		List<LocationGameStageResultView> stages = locationGameStageRepository.findAllBySessionIdOrderByStageNumber(sessionId)
			.stream()
			.map(stage -> {
				List<LocationGameAttemptResultView> attempts = attemptsByStageId.getOrDefault(stage.getId(), List.of());
				return new LocationGameStageResultView(
					stage.getStageNumber(),
					stage.getTargetCountryName(),
					stage.getTargetCountryIso3Code(),
					stage.getStatus(),
					stage.getAttemptCount(),
					stage.getAwardedScore(),
					stage.getClearedAt(),
					attempts
				);
			})
			.toList();
		int totalAttemptCount = attemptsByStageId.values().stream()
			.mapToInt(List::size)
			.sum();
		int firstTryClearCount = (int) stages.stream()
			.filter(stage -> stage.status() == LocationGameStageStatus.CLEARED)
			.filter(stage -> stage.attemptCount() == 1)
			.count();

		return new LocationGameSessionResultView(
			session.getId(),
			session.getPlayerNickname(),
			session.getStatus(),
			session.getTotalRounds(),
			session.getClearedStageCount(),
			totalAttemptCount,
			firstTryClearCount,
			session.getTotalScore(),
			session.getCurrentStageNumber(),
			session.getLivesRemaining(),
			session.getStartedAt(),
			session.getFinishedAt(),
			stages
		);
	}

	@Transactional(readOnly = true)
	public void assertSessionAccessible(UUID sessionId, GameSessionAccessContext accessContext) {
		getSession(sessionId, accessContext);
	}

	private LocationGameSession getSession(UUID sessionId) {
		return locationGameSessionRepository.findById(sessionId)
			.orElseThrow(() -> new ResourceNotFoundException("게임 세션을 찾을 수 없습니다: " + sessionId));
	}

	private LocationGameSession getSession(UUID sessionId, GameSessionAccessContext accessContext) {
		LocationGameSession session = getSession(sessionId);
		accessContext.assertCanAccess(session);
		return session;
	}

	private LocationGameStage getStage(UUID sessionId, Integer stageNumber) {
		return locationGameStageRepository.findBySessionIdAndStageNumber(sessionId, stageNumber)
			.orElseThrow(() -> new ResourceNotFoundException("게임 Stage를 찾을 수 없습니다."));
	}

	private void assertResultAccessible(LocationGameSession session) {
		if (session.getStatus() == GameSessionStatus.READY || session.getStatus() == GameSessionStatus.IN_PROGRESS) {
			throw new ResourceNotFoundException("게임 결과를 찾을 수 없습니다: " + session.getId());
		}
	}

	private String normalizeNickname(String nickname) {
		if (nickname == null || nickname.isBlank()) {
			return "Guest";
		}

		return nickname.trim();
	}

	private String normalizeCountryCode(String countryCode) {
		return countryCode.trim().toUpperCase(Locale.ROOT);
	}

	private LocationGameAttemptResultView toAttemptResultView(
		LocationGameSession session,
		LocationGameAttempt attempt,
		Map<Long, Country> countriesById,
		Map<String, Country> countriesByIso3Code
	) {
		return new LocationGameAttemptResultView(
			attempt.getAttemptNumber(),
			attempt.getSelectedCountryName(),
			attempt.getSelectedCountryIso3Code(),
			attempt.getCorrect(),
			attempt.getLivesRemainingAfter(),
			attempt.getAttemptedAt()
		);
	}

	private List<Country> getCountriesSortedByPopulation() {
		List<Country> countries = countryRepository.findAll()
			.stream()
			.sorted(
				Comparator.comparing(Country::getPopulation, Comparator.reverseOrder())
					.thenComparing(Country::getNameKr)
			)
			.toList();

		return countries.stream()
			.limit(LEVEL_ONE_COUNTRY_LIMIT)
			.toList();
	}

	private LocationGameDifficultyPlan resolveDifficulty(Integer stageNumber) {
		return locationGameDifficultyPolicy.resolve(stageNumber, LEVEL_ONE_COUNTRY_LIMIT);
	}

	private void createNextStage(
		LocationGameSession session,
		Integer stageNumber,
		List<Country> sortedCountries,
		List<LocationGameStage> existingStages
	) {
		if (locationGameStageRepository.findBySessionIdAndStageNumber(session.getId(), stageNumber).isPresent()) {
			return;
		}

		LocationGameDifficultyPlan difficultyPlan = locationGameDifficultyPolicy.resolve(stageNumber, sortedCountries.size());
		Country nextCountry = selectCountryForStage(sortedCountries, existingStages, difficultyPlan);
		locationGameStageRepository.save(LocationGameStage.create(session, stageNumber, nextCountry));
		session.planNextStage(stageNumber);
	}

	private Country selectCountryForStage(
		List<Country> sortedCountries,
		List<LocationGameStage> existingStages,
		LocationGameDifficultyPlan difficultyPlan
	) {
		Set<String> usedIso3Codes = new HashSet<>();
		String lastCountryIso3Code = null;

		for (LocationGameStage existingStage : existingStages) {
			usedIso3Codes.add(existingStage.getTargetCountryIso3Code());
			lastCountryIso3Code = existingStage.getTargetCountryIso3Code();
		}

		List<Country> difficultyPool = new ArrayList<>(sortedCountries.subList(0, difficultyPlan.candidatePoolSize()));
		List<Country> freshCandidates = difficultyPool.stream()
			.filter(country -> !usedIso3Codes.contains(country.getIso3Code()))
			.toList();

		if (!freshCandidates.isEmpty()) {
			return pickRandomCountry(freshCandidates);
		}

		List<Country> widerFreshCandidates = sortedCountries.stream()
			.filter(country -> !usedIso3Codes.contains(country.getIso3Code()))
			.toList();

		if (!widerFreshCandidates.isEmpty()) {
			return pickRandomCountry(widerFreshCandidates);
		}

		if (lastCountryIso3Code != null) {
			String recentCountryIso3Code = lastCountryIso3Code;
			List<Country> withoutImmediateRepeat = difficultyPool.stream()
				.filter(country -> !country.getIso3Code().equals(recentCountryIso3Code))
				.toList();

			if (!withoutImmediateRepeat.isEmpty()) {
				return pickRandomCountry(withoutImmediateRepeat);
			}
		}

		return pickRandomCountry(difficultyPool);
	}

	private Country pickRandomCountry(List<Country> candidates) {
		return candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
	}

	private LocationGameAnswerOutcome determineOutcome(boolean correct, GameSessionStatus status) {
		if (correct) {
			return status == GameSessionStatus.FINISHED
				? LocationGameAnswerOutcome.FINISHED
				: LocationGameAnswerOutcome.CORRECT;
		}

		return status == GameSessionStatus.GAME_OVER
			? LocationGameAnswerOutcome.GAME_OVER
			: LocationGameAnswerOutcome.WRONG;
	}
}
