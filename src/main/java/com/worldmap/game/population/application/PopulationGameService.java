package com.worldmap.game.population.application;

import com.worldmap.common.exception.ResourceNotFoundException;
import com.worldmap.country.domain.Country;
import com.worldmap.country.domain.CountryRepository;
import com.worldmap.game.common.domain.GameSessionStatus;
import com.worldmap.game.population.domain.PopulationGameAttempt;
import com.worldmap.game.population.domain.PopulationGameAttemptRepository;
import com.worldmap.game.population.domain.PopulationGameSession;
import com.worldmap.game.population.domain.PopulationGameSessionRepository;
import com.worldmap.game.population.domain.PopulationGameStage;
import com.worldmap.game.population.domain.PopulationGameStageRepository;
import com.worldmap.game.population.domain.PopulationGameStageStatus;
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
public class PopulationGameService {

	private static final int MINIMUM_COUNTRY_COUNT = 4;

	private final CountryRepository countryRepository;
	private final PopulationGameSessionRepository populationGameSessionRepository;
	private final PopulationGameStageRepository populationGameStageRepository;
	private final PopulationGameAttemptRepository populationGameAttemptRepository;
	private final PopulationGameOptionGenerator populationGameOptionGenerator;
	private final PopulationGameDifficultyPolicy populationGameDifficultyPolicy;
	private final PopulationGameScoringPolicy populationGameScoringPolicy;
	private final PopulationOptionLabelFormatter populationOptionLabelFormatter;
	private final LeaderboardService leaderboardService;

	public PopulationGameService(
		CountryRepository countryRepository,
		PopulationGameSessionRepository populationGameSessionRepository,
		PopulationGameStageRepository populationGameStageRepository,
		PopulationGameAttemptRepository populationGameAttemptRepository,
		PopulationGameOptionGenerator populationGameOptionGenerator,
		PopulationGameDifficultyPolicy populationGameDifficultyPolicy,
		PopulationGameScoringPolicy populationGameScoringPolicy,
		PopulationOptionLabelFormatter populationOptionLabelFormatter,
		LeaderboardService leaderboardService
	) {
		this.countryRepository = countryRepository;
		this.populationGameSessionRepository = populationGameSessionRepository;
		this.populationGameStageRepository = populationGameStageRepository;
		this.populationGameAttemptRepository = populationGameAttemptRepository;
		this.populationGameOptionGenerator = populationGameOptionGenerator;
		this.populationGameDifficultyPolicy = populationGameDifficultyPolicy;
		this.populationGameScoringPolicy = populationGameScoringPolicy;
		this.populationOptionLabelFormatter = populationOptionLabelFormatter;
		this.leaderboardService = leaderboardService;
	}

	@Transactional
	public PopulationGameStartView startGame(String nickname, String guestSessionKey) {
		List<Country> countries = getCountriesSortedByPopulation();

		if (countries.size() < MINIMUM_COUNTRY_COUNT) {
			throw new IllegalStateException("인구수 게임을 시작하기 위한 국가 데이터가 충분하지 않습니다.");
		}

		PopulationGameSession session = PopulationGameSession.ready(normalizeNickname(nickname), null, guestSessionKey, 1);
		session = populationGameSessionRepository.save(session);
		createNextStage(session, 1, countries, List.of());
		session.startGame(LocalDateTime.now());

		return new PopulationGameStartView(
			session.getId(),
			session.getPlayerNickname(),
			session.getStatus(),
			session.getTotalRounds(),
			session.getLivesRemaining(),
			"/games/population/play/" + session.getId()
		);
	}

	@Transactional(readOnly = true)
	public PopulationGameStateView getCurrentState(UUID sessionId) {
		PopulationGameSession session = getSession(sessionId);

		if (session.getStatus() != GameSessionStatus.IN_PROGRESS) {
			throw new IllegalStateException("이미 종료된 게임입니다.");
		}

		PopulationGameStage stage = getStage(sessionId, session.getCurrentStageNumber());
		PopulationGameDifficultyPlan difficultyPlan = resolveDifficulty(stage.getStageNumber());

		return new PopulationGameStateView(
			session.getId(),
			stage.getStageNumber(),
			difficultyPlan.label(),
			session.getClearedStageCount(),
			session.getTotalScore(),
			session.getLivesRemaining(),
			stage.getTargetCountryName(),
			stage.getPopulationYear(),
			toOptionViews(stage),
			session.getStatus()
		);
	}

	@Transactional
	public PopulationGameStartView restartGame(UUID sessionId) {
		PopulationGameSession session = getSession(sessionId);

		if (session.getStatus() != GameSessionStatus.GAME_OVER && session.getStatus() != GameSessionStatus.FINISHED) {
			throw new IllegalStateException("종료된 게임만 다시 시작할 수 있습니다.");
		}

		List<Country> countries = getCountriesSortedByPopulation();
		if (countries.size() < MINIMUM_COUNTRY_COUNT) {
			throw new IllegalStateException("인구수 게임을 시작하기 위한 국가 데이터가 충분하지 않습니다.");
		}

		populationGameAttemptRepository.deleteAllByStageSessionId(sessionId);
		populationGameAttemptRepository.flush();
		populationGameStageRepository.deleteAllBySessionId(sessionId);
		populationGameStageRepository.flush();

		session.restart(1);
		createNextStage(session, 1, countries, List.of());
		session.startGame(LocalDateTime.now());

		return new PopulationGameStartView(
			session.getId(),
			session.getPlayerNickname(),
			session.getStatus(),
			session.getTotalRounds(),
			session.getLivesRemaining(),
			"/games/population/play/" + session.getId()
		);
	}

	@Transactional
	public PopulationGameAnswerView submitAnswer(UUID sessionId, Integer stageNumber, Integer selectedOptionNumber) {
		PopulationGameSession session = getSession(sessionId);

		if (session.getStatus() != GameSessionStatus.IN_PROGRESS) {
			throw new IllegalStateException("진행 중인 게임만 답안을 제출할 수 있습니다.");
		}

		if (!session.getCurrentStageNumber().equals(stageNumber)) {
			throw new IllegalStateException("현재 진행 중인 Stage와 일치하지 않습니다.");
		}

		PopulationGameStage stage = getStage(sessionId, stageNumber);
		int attemptNumber = stage.nextAttemptNumber();
		LocalDateTime attemptedAt = LocalDateTime.now();
		PopulationAnswerJudgement judgement = populationGameScoringPolicy.judge(
			selectedOptionNumber,
			stage.getCorrectOptionNumber(),
			stageNumber,
			attemptNumber,
			session.getLivesRemaining()
		);
		Long selectedPopulation = stage.getOptions().get(selectedOptionNumber - 1);

		stage.recordAttempt(judgement.correct(), judgement.awardedScore(), attemptedAt);

		if (judgement.correct()) {
			List<Country> countries = getCountriesSortedByPopulation();
			List<PopulationGameStage> existingStages = populationGameStageRepository.findAllBySessionIdOrderByStageNumber(sessionId);
			createNextStage(session, stageNumber + 1, countries, existingStages);
			session.clearCurrentStage(stageNumber, judgement.awardedScore(), attemptedAt);
		} else {
			session.recordWrongAttempt(stageNumber, attemptedAt);

			if (session.getStatus() == GameSessionStatus.GAME_OVER) {
				stage.markFailed();
			}
		}

		populationGameAttemptRepository.save(
			PopulationGameAttempt.create(
				stage,
				attemptNumber,
				selectedOptionNumber,
				selectedPopulation,
				judgement.correct(),
				session.getLivesRemaining(),
				attemptedAt
			)
		);

		if (session.getStatus() != GameSessionStatus.IN_PROGRESS) {
			leaderboardService.recordPopulationLevelOneResult(
				session,
				Math.toIntExact(populationGameAttemptRepository.countByStageSessionId(sessionId))
			);
		}

		PopulationGameAnswerOutcome outcome = determineOutcome(judgement.correct(), session.getStatus());
		PopulationGameDifficultyPlan nextDifficultyPlan = session.getStatus() == GameSessionStatus.IN_PROGRESS
			? resolveDifficulty(session.getCurrentStageNumber())
			: null;

		return new PopulationGameAnswerView(
			session.getId(),
			stage.getStageNumber(),
			stage.getTargetCountryName(),
			stage.getPopulationYear(),
			selectedOptionNumber,
			selectedPopulation,
			populationOptionLabelFormatter.labelForLowerBound(selectedPopulation),
			stage.getCorrectOptionNumber(),
			stage.getTargetPopulation(),
			populationOptionLabelFormatter.labelForLowerBound(stage.getOptions().get(stage.getCorrectOptionNumber() - 1)),
			judgement.correct(),
			judgement.awardedScore(),
			session.getTotalScore(),
			session.getClearedStageCount(),
			session.getLivesRemaining(),
			session.getStatus() == GameSessionStatus.IN_PROGRESS ? session.getCurrentStageNumber() : null,
			nextDifficultyPlan != null ? nextDifficultyPlan.label() : null,
			session.getStatus(),
			outcome,
			"/games/population/result/" + session.getId()
		);
	}

	@Transactional(readOnly = true)
	public PopulationGameSessionResultView getSessionResult(UUID sessionId) {
		PopulationGameSession session = getSession(sessionId);
		Map<Long, List<PopulationGameAttemptResultView>> attemptsByStageId = new LinkedHashMap<>();

		populationGameAttemptRepository.findAllByStageSessionIdOrderByStageStageNumberAscAttemptNumberAsc(sessionId)
			.forEach(attempt -> attemptsByStageId.computeIfAbsent(attempt.getStage().getId(), ignored -> new ArrayList<>())
				.add(new PopulationGameAttemptResultView(
					attempt.getAttemptNumber(),
					attempt.getSelectedOptionNumber(),
					attempt.getSelectedPopulation(),
					populationOptionLabelFormatter.labelForLowerBound(attempt.getSelectedPopulation()),
					attempt.getCorrect(),
					attempt.getLivesRemainingAfter(),
					attempt.getAttemptedAt()
				)));

		List<PopulationGameStageResultView> stages = populationGameStageRepository.findAllBySessionIdOrderByStageNumber(sessionId)
			.stream()
			.map(stage -> new PopulationGameStageResultView(
				stage.getStageNumber(),
				stage.getTargetCountryName(),
				stage.getPopulationYear(),
				stage.getTargetPopulation(),
				populationOptionLabelFormatter.labelForLowerBound(stage.getOptions().get(stage.getCorrectOptionNumber() - 1)),
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
			.filter(stage -> stage.status() == PopulationGameStageStatus.CLEARED)
			.filter(stage -> stage.attemptCount() == 1)
			.count();

		return new PopulationGameSessionResultView(
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

	private PopulationGameSession getSession(UUID sessionId) {
		return populationGameSessionRepository.findById(sessionId)
			.orElseThrow(() -> new ResourceNotFoundException("게임 세션을 찾을 수 없습니다: " + sessionId));
	}

	private PopulationGameStage getStage(UUID sessionId, Integer stageNumber) {
		return populationGameStageRepository.findBySessionIdAndStageNumber(sessionId, stageNumber)
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
			.sorted(
				Comparator.comparing(Country::getPopulation, Comparator.reverseOrder())
					.thenComparing(Country::getNameKr)
			)
			.toList();
	}

	private PopulationGameDifficultyPlan resolveDifficulty(Integer stageNumber) {
		return populationGameDifficultyPolicy.resolve(stageNumber, (int) countryRepository.count());
	}

	private List<PopulationOptionView> toOptionViews(PopulationGameStage stage) {
		List<PopulationOptionView> optionViews = new ArrayList<>();
		List<Long> options = stage.getOptions();

		for (int index = 0; index < options.size(); index++) {
			Long population = options.get(index);
			optionViews.add(new PopulationOptionView(
				index + 1,
				population,
				populationOptionLabelFormatter.labelForLowerBound(population)
			));
		}

		return List.copyOf(optionViews);
	}

	private void createNextStage(
		PopulationGameSession session,
		Integer stageNumber,
		List<Country> sortedCountries,
		List<PopulationGameStage> existingStages
	) {
		if (populationGameStageRepository.findBySessionIdAndStageNumber(session.getId(), stageNumber).isPresent()) {
			return;
		}

		PopulationGameDifficultyPlan difficultyPlan = populationGameDifficultyPolicy.resolve(stageNumber, sortedCountries.size());
		List<Country> difficultyPool = new ArrayList<>(sortedCountries.subList(0, difficultyPlan.candidatePoolSize()));
		Country nextCountry = selectCountryForStage(sortedCountries, existingStages, difficultyPool);
		PopulationRoundOptions roundOptions = populationGameOptionGenerator.generate(nextCountry, sortedCountries);
		populationGameStageRepository.save(
			PopulationGameStage.create(
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
		List<PopulationGameStage> existingStages,
		List<Country> difficultyPool
	) {
		Set<String> usedIso3Codes = new HashSet<>();
		String lastCountryIso3Code = null;

		for (PopulationGameStage existingStage : existingStages) {
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

	private PopulationGameAnswerOutcome determineOutcome(boolean correct, GameSessionStatus status) {
		if (correct) {
			return status == GameSessionStatus.FINISHED
				? PopulationGameAnswerOutcome.FINISHED
				: PopulationGameAnswerOutcome.CORRECT;
		}

		return status == GameSessionStatus.GAME_OVER
			? PopulationGameAnswerOutcome.GAME_OVER
			: PopulationGameAnswerOutcome.WRONG;
	}
}
