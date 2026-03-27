package com.worldmap.stats.application;

import com.worldmap.auth.domain.MemberRepository;
import com.worldmap.game.capital.domain.CapitalGameSessionRepository;
import com.worldmap.game.flag.domain.FlagGameSessionRepository;
import com.worldmap.game.location.domain.LocationGameSessionRepository;
import com.worldmap.game.population.domain.PopulationGameSessionRepository;
import com.worldmap.game.populationbattle.domain.PopulationBattleGameSessionRepository;
import com.worldmap.ranking.domain.LeaderboardGameMode;
import com.worldmap.ranking.domain.LeaderboardRecordRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServiceActivityService {

	private final MemberRepository memberRepository;
	private final CapitalGameSessionRepository capitalGameSessionRepository;
	private final FlagGameSessionRepository flagGameSessionRepository;
	private final LocationGameSessionRepository locationGameSessionRepository;
	private final PopulationGameSessionRepository populationGameSessionRepository;
	private final PopulationBattleGameSessionRepository populationBattleGameSessionRepository;
	private final LeaderboardRecordRepository leaderboardRecordRepository;

	public ServiceActivityService(
		MemberRepository memberRepository,
		CapitalGameSessionRepository capitalGameSessionRepository,
		FlagGameSessionRepository flagGameSessionRepository,
		LocationGameSessionRepository locationGameSessionRepository,
		PopulationGameSessionRepository populationGameSessionRepository,
		PopulationBattleGameSessionRepository populationBattleGameSessionRepository,
		LeaderboardRecordRepository leaderboardRecordRepository
	) {
		this.memberRepository = memberRepository;
		this.capitalGameSessionRepository = capitalGameSessionRepository;
		this.flagGameSessionRepository = flagGameSessionRepository;
		this.locationGameSessionRepository = locationGameSessionRepository;
		this.populationGameSessionRepository = populationGameSessionRepository;
		this.populationBattleGameSessionRepository = populationBattleGameSessionRepository;
		this.leaderboardRecordRepository = leaderboardRecordRepository;
	}

	@Transactional(readOnly = true)
	public ServiceActivityView loadTodayActivity() {
		LocalDateTime todayStart = LocalDate.now().atStartOfDay();
		LocalDateTime tomorrowStart = todayStart.plusDays(1);

		Set<Long> activeMemberIds = Stream.concat(
			locationGameSessionRepository.findDistinctMemberIdsByStartedAtBetween(todayStart, tomorrowStart).stream(),
			Stream.concat(
				populationGameSessionRepository.findDistinctMemberIdsByStartedAtBetween(todayStart, tomorrowStart).stream(),
				Stream.concat(
					capitalGameSessionRepository.findDistinctMemberIdsByStartedAtBetween(todayStart, tomorrowStart).stream(),
					java.util.stream.Stream.concat(
						flagGameSessionRepository.findDistinctMemberIdsByStartedAtBetween(todayStart, tomorrowStart).stream(),
						populationBattleGameSessionRepository.findDistinctMemberIdsByStartedAtBetween(todayStart, tomorrowStart).stream()
					)
				)
			)
		).collect(Collectors.toSet());

		Set<String> activeGuestKeys = Stream.concat(
			locationGameSessionRepository.findDistinctGuestSessionKeysByStartedAtBetween(todayStart, tomorrowStart).stream(),
			Stream.concat(
				populationGameSessionRepository.findDistinctGuestSessionKeysByStartedAtBetween(todayStart, tomorrowStart).stream(),
				Stream.concat(
					capitalGameSessionRepository.findDistinctGuestSessionKeysByStartedAtBetween(todayStart, tomorrowStart).stream(),
					java.util.stream.Stream.concat(
						flagGameSessionRepository.findDistinctGuestSessionKeysByStartedAtBetween(todayStart, tomorrowStart).stream(),
						populationBattleGameSessionRepository.findDistinctGuestSessionKeysByStartedAtBetween(todayStart, tomorrowStart).stream()
					)
				)
			)
		).collect(Collectors.toSet());

		long todayStartedSessionCount =
			locationGameSessionRepository.countByStartedAtGreaterThanEqualAndStartedAtLessThan(todayStart, tomorrowStart)
				+ capitalGameSessionRepository.countByStartedAtGreaterThanEqualAndStartedAtLessThan(todayStart, tomorrowStart)
				+ flagGameSessionRepository.countByStartedAtGreaterThanEqualAndStartedAtLessThan(todayStart, tomorrowStart)
				+ populationBattleGameSessionRepository.countByStartedAtGreaterThanEqualAndStartedAtLessThan(todayStart, tomorrowStart)
				+ populationGameSessionRepository.countByStartedAtGreaterThanEqualAndStartedAtLessThan(todayStart, tomorrowStart);

		long todayCompletedRunCount = leaderboardRecordRepository.countByFinishedAtGreaterThanEqualAndFinishedAtLessThan(
			todayStart,
			tomorrowStart
		);

		return new ServiceActivityView(
			memberRepository.count(),
			activeMemberIds.size(),
			activeGuestKeys.size(),
			todayStartedSessionCount,
			todayCompletedRunCount,
			leaderboardRecordRepository.countByGameModeAndFinishedAtGreaterThanEqualAndFinishedAtLessThan(
				LeaderboardGameMode.LOCATION,
				todayStart,
				tomorrowStart
			),
			leaderboardRecordRepository.countByGameModeAndFinishedAtGreaterThanEqualAndFinishedAtLessThan(
				LeaderboardGameMode.CAPITAL,
				todayStart,
				tomorrowStart
			),
			leaderboardRecordRepository.countByGameModeAndFinishedAtGreaterThanEqualAndFinishedAtLessThan(
				LeaderboardGameMode.FLAG,
				todayStart,
				tomorrowStart
			),
			leaderboardRecordRepository.countByGameModeAndFinishedAtGreaterThanEqualAndFinishedAtLessThan(
				LeaderboardGameMode.POPULATION_BATTLE,
				todayStart,
				tomorrowStart
			),
			leaderboardRecordRepository.countByGameModeAndFinishedAtGreaterThanEqualAndFinishedAtLessThan(
				LeaderboardGameMode.POPULATION,
				todayStart,
				tomorrowStart
			)
		);
	}
}
