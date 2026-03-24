package com.worldmap.stats.application;

import com.worldmap.auth.domain.MemberRepository;
import com.worldmap.game.location.domain.LocationGameSessionRepository;
import com.worldmap.game.population.domain.PopulationGameSessionRepository;
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
	private final LocationGameSessionRepository locationGameSessionRepository;
	private final PopulationGameSessionRepository populationGameSessionRepository;
	private final LeaderboardRecordRepository leaderboardRecordRepository;

	public ServiceActivityService(
		MemberRepository memberRepository,
		LocationGameSessionRepository locationGameSessionRepository,
		PopulationGameSessionRepository populationGameSessionRepository,
		LeaderboardRecordRepository leaderboardRecordRepository
	) {
		this.memberRepository = memberRepository;
		this.locationGameSessionRepository = locationGameSessionRepository;
		this.populationGameSessionRepository = populationGameSessionRepository;
		this.leaderboardRecordRepository = leaderboardRecordRepository;
	}

	@Transactional(readOnly = true)
	public ServiceActivityView loadTodayActivity() {
		LocalDateTime todayStart = LocalDate.now().atStartOfDay();
		LocalDateTime tomorrowStart = todayStart.plusDays(1);

		Set<Long> activeMemberIds = Stream.concat(
			locationGameSessionRepository.findDistinctMemberIdsByStartedAtBetween(todayStart, tomorrowStart).stream(),
			populationGameSessionRepository.findDistinctMemberIdsByStartedAtBetween(todayStart, tomorrowStart).stream()
		).collect(Collectors.toSet());

		Set<String> activeGuestKeys = Stream.concat(
			locationGameSessionRepository.findDistinctGuestSessionKeysByStartedAtBetween(todayStart, tomorrowStart).stream(),
			populationGameSessionRepository.findDistinctGuestSessionKeysByStartedAtBetween(todayStart, tomorrowStart).stream()
		).collect(Collectors.toSet());

		long todayStartedSessionCount =
			locationGameSessionRepository.countByStartedAtGreaterThanEqualAndStartedAtLessThan(todayStart, tomorrowStart)
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
				LeaderboardGameMode.POPULATION,
				todayStart,
				tomorrowStart
			)
		);
	}
}
