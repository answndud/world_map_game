package com.worldmap.game.capital.application;

import com.worldmap.common.exception.ResourceNotFoundException;
import com.worldmap.country.domain.Country;
import com.worldmap.country.domain.CountryRepository;
import com.worldmap.game.capital.domain.CapitalGameAttempt;
import com.worldmap.game.capital.domain.CapitalGameAttemptRepository;
import com.worldmap.game.capital.domain.CapitalGameSession;
import com.worldmap.game.capital.domain.CapitalGameSessionRepository;
import com.worldmap.game.capital.domain.CapitalGameStage;
import com.worldmap.game.capital.domain.CapitalGameStageRepository;
import com.worldmap.game.capital.domain.CapitalGameStageStatus;
import com.worldmap.game.common.domain.GameSessionStatus;
import com.worldmap.ranking.application.LeaderboardService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CapitalGameService {

	private static final int MINIMUM_COUNTRY_COUNT = 4;

	private final CountryRepository countryRepository;
	private final CapitalGameSessionRepository capitalGameSessionRepository;
	private final CapitalGameStageRepository capitalGameStageRepository;
	private final CapitalGameAttemptRepository capitalGameAttemptRepository;
	private final CapitalGameOptionGenerator capitalGameOptionGenerator;
	private final CapitalGameDifficultyPolicy capitalGameDifficultyPolicy;
	private final CapitalGameScoringPolicy capitalGameScoringPolicy;
	private final LeaderboardService leaderboardService;

	public CapitalGameService(
		CountryRepository countryRepository,
		CapitalGameSessionRepository capitalGameSessionRepository,
		CapitalGameStageRepository capitalGameStageRepository,
		CapitalGameAttemptRepository capitalGameAttemptRepository,
		CapitalGameOptionGenerator capitalGameOptionGenerator,
		CapitalGameDifficultyPolicy capitalGameDifficultyPolicy,
		CapitalGameScoringPolicy capitalGameScoringPolicy,
		LeaderboardService leaderboardService
	) {
		this.countryRepository = countryRepository;
		this.capitalGameSessionRepository = capitalGameSessionRepository;
		this.capitalGameStageRepository = capitalGameStageRepository;
		this.capitalGameAttemptRepository = capitalGameAttemptRepository;
		this.capitalGameOptionGenerator = capitalGameOptionGenerator;
		this.capitalGameDifficultyPolicy = capitalGameDifficultyPolicy;
		this.capitalGameScoringPolicy = capitalGameScoringPolicy;
		this.leaderboardService = leaderboardService;
	}

	@Transactional
	public CapitalGameStartView startGuestGame(String nickname, String guestSessionKey) {
		return startGame(normalizeNickname(nickname), null, guestSessionKey);
	}

	@Transactional
	public CapitalGameStartView startMemberGame(Long memberId, String memberNickname) {
		return startGame(memberNickname, memberId, null);
	}

	private CapitalGameStartView startGame(
		String playerNickname,
		Long memberId,
		String guestSessionKey
	) {
		List<Country> countries = getCountriesSortedByPopulation();

		if (countries.size() < MINIMUM_COUNTRY_COUNT) {
			throw new IllegalStateException("수도 맞히기 게임을 시작하기 위한 국가 데이터가 충분하지 않습니다.");
		}

		CapitalGameSession session = CapitalGameSession.ready(playerNickname, memberId, guestSessionKey, 1);
		session = capitalGameSessionRepository.save(session);
		createNextStage(session, 1, countries, List.of());
		session.startGame(LocalDateTime.now());

		return new CapitalGameStartView(
			session.getId(),
			session.getPlayerNickname(),
			session.getStatus(),
			session.getTotalRounds(),
			session.getLivesRemaining(),
			"/games/capital/play/" + session.getId()
		);
	}

	@Transactional(readOnly = true)
	public CapitalGameStateView getCurrentState(UUID sessionId) {
		CapitalGameSession session = getSession(sessionId);

		if (session.getStatus() != GameSessionStatus.IN_PROGRESS) {
			throw new IllegalStateException("이미 종료된 게임입니다.");
		}

		CapitalGameStage stage = getStage(sessionId, session.getCurrentStageNumber());
		CapitalGameDifficultyPlan difficultyPlan = resolveDifficulty(stage.getStageNumber());

		return new CapitalGameStateView(
			session.getId(),
			stage.getStageNumber(),
			difficultyPlan.label(),
			session.getClearedStageCount(),
			session.getTotalScore(),
			session.getLivesRemaining(),
			stage.getTargetCountryName(),
			toOptionViews(stage),
			session.getStatus()
		);
	}

	@Transactional
	public CapitalGameStartView restartGame(UUID sessionId) {
		CapitalGameSession session = getSession(sessionId);

		if (session.getStatus() != GameSessionStatus.GAME_OVER && session.getStatus() != GameSessionStatus.FINISHED) {
			throw new IllegalStateException("종료된 게임만 다시 시작할 수 있습니다.");
		}

		List<Country> countries = getCountriesSortedByPopulation();
		if (countries.size() < MINIMUM_COUNTRY_COUNT) {
			throw new IllegalStateException("수도 맞히기 게임을 시작하기 위한 국가 데이터가 충분하지 않습니다.");
		}

		capitalGameAttemptRepository.deleteAllByStageSessionId(sessionId);
		capitalGameAttemptRepository.flush();
		capitalGameStageRepository.deleteAllBySessionId(sessionId);
		capitalGameStageRepository.flush();

		session.restart(1);
		createNextStage(session, 1, countries, List.of());
		session.startGame(LocalDateTime.now());

		return new CapitalGameStartView(
			session.getId(),
			session.getPlayerNickname(),
			session.getStatus(),
			session.getTotalRounds(),
			session.getLivesRemaining(),
			"/games/capital/play/" + session.getId()
		);
	}

	@Transactional
	public CapitalGameAnswerView submitAnswer(UUID sessionId, Integer stageNumber, Integer selectedOptionNumber) {
		if (selectedOptionNumber == null || selectedOptionNumber < 1 || selectedOptionNumber > 4) {
			throw new IllegalArgumentException("보기 번호를 선택해야 합니다.");
		}

		CapitalGameSession session = getSession(sessionId);
		if (session.getStatus() != GameSessionStatus.IN_PROGRESS) {
			throw new IllegalStateException("진행 중인 게임만 답안을 제출할 수 있습니다.");
		}

		if (!session.getCurrentStageNumber().equals(stageNumber)) {
			throw new IllegalStateException("현재 진행 중인 Stage와 일치하지 않습니다.");
		}

		CapitalGameStage stage = getStage(sessionId, stageNumber);
		int attemptNumber = stage.nextAttemptNumber();
		LocalDateTime attemptedAt = LocalDateTime.now();
		CapitalAnswerJudgement judgement = capitalGameScoringPolicy.judge(
			selectedOptionNumber,
			stage.getCorrectOptionNumber(),
			stageNumber,
			attemptNumber,
			session.getLivesRemaining()
		);

		String selectedCapitalCity = stage.getOptions().get(selectedOptionNumber - 1);
		String correctCapitalCity = stage.getOptions().get(stage.getCorrectOptionNumber() - 1);

		stage.recordAttempt(judgement.correct(), judgement.awardedScore(), attemptedAt);

		if (judgement.correct()) {
			List<Country> countries = getCountriesSortedByPopulation();
			List<CapitalGameStage> existingStages = capitalGameStageRepository.findAllBySessionIdOrderByStageNumber(sessionId);
			createNextStage(session, stageNumber + 1, countries, existingStages);
			session.clearCurrentStage(stageNumber, judgement.awardedScore(), attemptedAt);
		} else {
			session.recordWrongAttempt(stageNumber, attemptedAt);
			if (session.getStatus() == GameSessionStatus.GAME_OVER) {
				stage.markFailed();
			}
		}

		capitalGameAttemptRepository.save(
			CapitalGameAttempt.create(
				stage,
				attemptNumber,
				selectedOptionNumber,
				selectedCapitalCity,
				judgement.correct(),
				session.getLivesRemaining(),
				attemptedAt
			)
		);

		if (session.getStatus() != GameSessionStatus.IN_PROGRESS) {
			leaderboardService.recordCapitalResult(
				session,
				Math.toIntExact(capitalGameAttemptRepository.countByStageSessionId(sessionId))
			);
		}

		CapitalGameAnswerOutcome outcome = determineOutcome(judgement.correct(), session.getStatus());
		CapitalGameDifficultyPlan nextDifficultyPlan = session.getStatus() == GameSessionStatus.IN_PROGRESS
			? resolveDifficulty(session.getCurrentStageNumber())
			: null;

		return new CapitalGameAnswerView(
			session.getId(),
			stage.getStageNumber(),
			stage.getTargetCountryName(),
			selectedOptionNumber,
			selectedCapitalCity,
			stage.getCorrectOptionNumber(),
			correctCapitalCity,
			judgement.correct(),
			judgement.awardedScore(),
			session.getTotalScore(),
			session.getClearedStageCount(),
			session.getLivesRemaining(),
			session.getStatus() == GameSessionStatus.IN_PROGRESS ? session.getCurrentStageNumber() : null,
			nextDifficultyPlan != null ? nextDifficultyPlan.label() : null,
			session.getStatus(),
			outcome,
			"/games/capital/result/" + session.getId()
		);
	}

	@Transactional(readOnly = true)
	public CapitalGameSessionResultView getSessionResult(UUID sessionId) {
		CapitalGameSession session = getSession(sessionId);
		Map<Long, List<CapitalGameAttemptResultView>> attemptsByStageId = new LinkedHashMap<>();

		capitalGameAttemptRepository.findAllByStageSessionIdOrderByStageStageNumberAscAttemptNumberAsc(sessionId)
			.forEach(attempt -> attemptsByStageId.computeIfAbsent(attempt.getStage().getId(), ignored -> new ArrayList<>())
				.add(new CapitalGameAttemptResultView(
					attempt.getAttemptNumber(),
					attempt.getSelectedOptionNumber(),
					attempt.getSelectedCapitalCity(),
					attempt.getCorrect(),
					attempt.getLivesRemainingAfter(),
					attempt.getAttemptedAt()
				)));

		List<CapitalGameStageResultView> stages = capitalGameStageRepository.findAllBySessionIdOrderByStageNumber(sessionId)
			.stream()
			.map(stage -> new CapitalGameStageResultView(
				stage.getStageNumber(),
				stage.getTargetCountryName(),
				stage.getTargetCapitalCity(),
				stage.getStatus(),
				stage.getAttemptCount(),
				stage.getAwardedScore(),
				stage.getClearedAt(),
				attemptsByStageId.getOrDefault(stage.getId(), List.of())
			))
			.toList();
		int totalAttemptCount = attemptsByStageId.values().stream()
			.mapToInt(List::size)
			.sum();
		int firstTryClearCount = (int) stages.stream()
			.filter(stage -> stage.status() == CapitalGameStageStatus.CLEARED)
			.filter(stage -> stage.attemptCount() == 1)
			.count();

		return new CapitalGameSessionResultView(
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

	private CapitalGameSession getSession(UUID sessionId) {
		return capitalGameSessionRepository.findById(sessionId)
			.orElseThrow(() -> new ResourceNotFoundException("게임 세션을 찾을 수 없습니다: " + sessionId));
	}

	private CapitalGameStage getStage(UUID sessionId, Integer stageNumber) {
		return capitalGameStageRepository.findBySessionIdAndStageNumber(sessionId, stageNumber)
			.orElseThrow(() -> new ResourceNotFoundException("게임 Stage를 찾을 수 없습니다."));
	}

	private String normalizeNickname(String nickname) {
		if (nickname == null || nickname.isBlank()) {
			return "Guest";
		}

		return nickname.trim();
	}

	private List<Country> getCountriesSortedByPopulation() {
		return countryRepository.findAll()
			.stream()
			.filter(country -> country.getCapitalCity() != null && !country.getCapitalCity().isBlank())
			.sorted(
				Comparator.comparing(Country::getPopulation, Comparator.reverseOrder())
					.thenComparing(Country::getNameKr)
			)
			.toList();
	}

	private CapitalGameDifficultyPlan resolveDifficulty(Integer stageNumber) {
		return capitalGameDifficultyPolicy.resolve(stageNumber, (int) getCountriesSortedByPopulation().size());
	}

	private List<CapitalOptionView> toOptionViews(CapitalGameStage stage) {
		List<CapitalOptionView> optionViews = new ArrayList<>();
		List<String> options = stage.getOptions();

		for (int index = 0; index < options.size(); index++) {
			optionViews.add(new CapitalOptionView(index + 1, options.get(index)));
		}

		return List.copyOf(optionViews);
	}

	private void createNextStage(
		CapitalGameSession session,
		Integer stageNumber,
		List<Country> sortedCountries,
		List<CapitalGameStage> existingStages
	) {
		if (capitalGameStageRepository.findBySessionIdAndStageNumber(session.getId(), stageNumber).isPresent()) {
			return;
		}

		CapitalGameDifficultyPlan difficultyPlan = capitalGameDifficultyPolicy.resolve(stageNumber, sortedCountries.size());
		List<Country> difficultyPool = new ArrayList<>(sortedCountries.subList(0, difficultyPlan.candidatePoolSize()));
		Country nextCountry = selectCountryForStage(sortedCountries, existingStages, difficultyPool);
		CapitalRoundOptions roundOptions = capitalGameOptionGenerator.generate(nextCountry, sortedCountries);
		capitalGameStageRepository.save(
			CapitalGameStage.create(
				session,
				stageNumber,
				nextCountry,
				roundOptions.options(),
				roundOptions.correctOptionNumber()
			)
		);
		session.planNextStage(stageNumber);
	}

	private Country selectCountryForStage(
		List<Country> sortedCountries,
		List<CapitalGameStage> existingStages,
		List<Country> difficultyPool
	) {
		Set<String> usedIso3Codes = new HashSet<>();
		String lastCountryIso3Code = null;

		for (CapitalGameStage existingStage : existingStages) {
			usedIso3Codes.add(existingStage.getCountryIso3Code());
			lastCountryIso3Code = existingStage.getCountryIso3Code();
		}

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

	private CapitalGameAnswerOutcome determineOutcome(boolean correct, GameSessionStatus status) {
		if (correct) {
			return status == GameSessionStatus.FINISHED
				? CapitalGameAnswerOutcome.FINISHED
				: CapitalGameAnswerOutcome.CORRECT;
		}

		return status == GameSessionStatus.GAME_OVER
			? CapitalGameAnswerOutcome.GAME_OVER
			: CapitalGameAnswerOutcome.WRONG;
	}
}
