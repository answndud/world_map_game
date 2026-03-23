package com.worldmap.game.population.application;

import com.worldmap.common.exception.ResourceNotFoundException;
import com.worldmap.country.domain.Country;
import com.worldmap.country.domain.CountryRepository;
import com.worldmap.game.common.domain.GameSessionStatus;
import com.worldmap.game.population.domain.PopulationGameRound;
import com.worldmap.game.population.domain.PopulationGameRoundRepository;
import com.worldmap.game.population.domain.PopulationGameSession;
import com.worldmap.game.population.domain.PopulationGameSessionRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PopulationGameService {

	private static final int MINIMUM_COUNTRY_COUNT = 4;
	private static final int DEFAULT_ROUND_COUNT = 5;

	private final CountryRepository countryRepository;
	private final PopulationGameSessionRepository populationGameSessionRepository;
	private final PopulationGameRoundRepository populationGameRoundRepository;
	private final PopulationGameOptionGenerator populationGameOptionGenerator;
	private final PopulationGameScoringPolicy populationGameScoringPolicy;

	public PopulationGameService(
		CountryRepository countryRepository,
		PopulationGameSessionRepository populationGameSessionRepository,
		PopulationGameRoundRepository populationGameRoundRepository,
		PopulationGameOptionGenerator populationGameOptionGenerator,
		PopulationGameScoringPolicy populationGameScoringPolicy
	) {
		this.countryRepository = countryRepository;
		this.populationGameSessionRepository = populationGameSessionRepository;
		this.populationGameRoundRepository = populationGameRoundRepository;
		this.populationGameOptionGenerator = populationGameOptionGenerator;
		this.populationGameScoringPolicy = populationGameScoringPolicy;
	}

	@Transactional
	public PopulationGameStartView startGame(String nickname) {
		List<Country> countries = new ArrayList<>(countryRepository.findAll());

		if (countries.size() < MINIMUM_COUNTRY_COUNT) {
			throw new IllegalStateException("인구수 게임을 시작하기 위한 국가 데이터가 충분하지 않습니다.");
		}

		Collections.shuffle(countries);
		int totalRounds = Math.min(DEFAULT_ROUND_COUNT, countries.size());
		PopulationGameSession session = PopulationGameSession.ready(normalizeNickname(nickname), totalRounds);
		populationGameSessionRepository.save(session);

		for (int index = 0; index < totalRounds; index++) {
			Country targetCountry = countries.get(index);
			PopulationRoundOptions roundOptions = populationGameOptionGenerator.generate(targetCountry, countries);
			populationGameRoundRepository.save(
				PopulationGameRound.create(
					session,
					index + 1,
					targetCountry,
					roundOptions.options(),
					roundOptions.correctOptionNumber()
				)
			);
		}

		session.startGame(LocalDateTime.now());

		return new PopulationGameStartView(
			session.getId(),
			session.getPlayerNickname(),
			session.getStatus(),
			session.getTotalRounds(),
			"/games/population/play/" + session.getId()
		);
	}

	@Transactional(readOnly = true)
	public PopulationGameCurrentRoundView getCurrentRound(UUID sessionId) {
		PopulationGameSession session = getSession(sessionId);

		if (session.getStatus() == GameSessionStatus.FINISHED) {
			throw new IllegalStateException("이미 종료된 게임입니다.");
		}

		PopulationGameRound round = getRound(sessionId, session.getCurrentRoundNumber());

		return new PopulationGameCurrentRoundView(
			session.getId(),
			round.getRoundNumber(),
			session.getTotalRounds(),
			session.getAnsweredRoundCount(),
			session.getTotalScore(),
			round.getTargetCountryName(),
			round.getPopulationYear(),
			toOptionViews(round)
		);
	}

	@Transactional
	public PopulationGameAnswerView submitAnswer(UUID sessionId, Integer roundNumber, Integer selectedOptionNumber) {
		if (selectedOptionNumber < 1 || selectedOptionNumber > 4) {
			throw new IllegalArgumentException("selectedOptionNumber는 1에서 4 사이여야 합니다.");
		}

		PopulationGameSession session = getSession(sessionId);

		if (session.getStatus() != GameSessionStatus.IN_PROGRESS) {
			throw new IllegalStateException("진행 중인 게임만 답안을 제출할 수 있습니다.");
		}

		if (!session.getCurrentRoundNumber().equals(roundNumber)) {
			throw new IllegalStateException("현재 진행 중인 라운드와 일치하지 않습니다.");
		}

		PopulationGameRound round = getRound(sessionId, roundNumber);
		PopulationAnswerJudgement judgement = populationGameScoringPolicy.judge(selectedOptionNumber, round.getCorrectOptionNumber());
		Long selectedPopulation = round.getOptions().get(selectedOptionNumber - 1);

		round.submit(
			selectedOptionNumber,
			selectedPopulation,
			judgement.correct(),
			judgement.awardedScore(),
			LocalDateTime.now()
		);
		session.completeRound(roundNumber, judgement.awardedScore(), LocalDateTime.now());

		return new PopulationGameAnswerView(
			session.getId(),
			round.getRoundNumber(),
			round.getTargetCountryName(),
			round.getPopulationYear(),
			round.getSelectedOptionNumber(),
			round.getSelectedPopulation(),
			round.getCorrectOptionNumber(),
			round.getTargetPopulation(),
			round.getCorrect(),
			round.getAwardedScore(),
			session.getTotalScore(),
			session.getAnsweredRoundCount(),
			session.getTotalRounds() - session.getAnsweredRoundCount(),
			session.getStatus() == GameSessionStatus.FINISHED ? null : session.getCurrentRoundNumber(),
			session.getStatus(),
			"/games/population/result/" + session.getId()
		);
	}

	@Transactional(readOnly = true)
	public PopulationGameSessionResultView getSessionResult(UUID sessionId) {
		PopulationGameSession session = getSession(sessionId);
		List<PopulationGameRoundResultView> rounds = populationGameRoundRepository.findAllBySessionIdOrderByRoundNumber(sessionId)
			.stream()
			.map(round -> new PopulationGameRoundResultView(
				round.getRoundNumber(),
				round.getTargetCountryName(),
				round.getPopulationYear(),
				round.getTargetPopulation(),
				round.getSelectedOptionNumber(),
				round.getSelectedPopulation(),
				round.getCorrectOptionNumber(),
				round.getTargetPopulation(),
				round.getCorrect(),
				round.getAwardedScore(),
				round.getAnsweredAt()
			))
			.toList();

		return new PopulationGameSessionResultView(
			session.getId(),
			session.getPlayerNickname(),
			session.getStatus(),
			session.getTotalRounds(),
			session.getAnsweredRoundCount(),
			session.getTotalScore(),
			session.getCurrentRoundNumber(),
			session.getStartedAt(),
			session.getFinishedAt(),
			rounds
		);
	}

	private PopulationGameSession getSession(UUID sessionId) {
		return populationGameSessionRepository.findById(sessionId)
			.orElseThrow(() -> new ResourceNotFoundException("게임 세션을 찾을 수 없습니다: " + sessionId));
	}

	private PopulationGameRound getRound(UUID sessionId, Integer roundNumber) {
		return populationGameRoundRepository.findBySessionIdAndRoundNumber(sessionId, roundNumber)
			.orElseThrow(() -> new ResourceNotFoundException("게임 라운드를 찾을 수 없습니다."));
	}

	private String normalizeNickname(String nickname) {
		if (nickname == null || nickname.isBlank()) {
			return "Guest";
		}

		return nickname.trim();
	}

	private List<PopulationOptionView> toOptionViews(PopulationGameRound round) {
		List<Long> options = round.getOptions();
		List<PopulationOptionView> optionViews = new ArrayList<>();

		for (int index = 0; index < options.size(); index++) {
			optionViews.add(new PopulationOptionView(index + 1, options.get(index)));
		}

		return List.copyOf(optionViews);
	}
}
