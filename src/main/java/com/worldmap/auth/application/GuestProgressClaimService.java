package com.worldmap.auth.application;

import com.worldmap.game.location.domain.LocationGameSession;
import com.worldmap.game.location.domain.LocationGameSessionRepository;
import com.worldmap.game.population.domain.PopulationGameSession;
import com.worldmap.game.population.domain.PopulationGameSessionRepository;
import com.worldmap.ranking.domain.LeaderboardRecord;
import com.worldmap.ranking.domain.LeaderboardRecordRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GuestProgressClaimService {

	private final LocationGameSessionRepository locationGameSessionRepository;
	private final PopulationGameSessionRepository populationGameSessionRepository;
	private final LeaderboardRecordRepository leaderboardRecordRepository;

	public GuestProgressClaimService(
		LocationGameSessionRepository locationGameSessionRepository,
		PopulationGameSessionRepository populationGameSessionRepository,
		LeaderboardRecordRepository leaderboardRecordRepository
	) {
		this.locationGameSessionRepository = locationGameSessionRepository;
		this.populationGameSessionRepository = populationGameSessionRepository;
		this.leaderboardRecordRepository = leaderboardRecordRepository;
	}

	@Transactional
	public void claimGuestRecords(Long memberId, String guestSessionKey) {
		if (memberId == null || guestSessionKey == null || guestSessionKey.isBlank()) {
			return;
		}

		List<LocationGameSession> locationSessions =
			locationGameSessionRepository.findAllByGuestSessionKeyAndMemberIdIsNull(guestSessionKey);
		locationSessions.forEach(session -> session.claimOwnership(memberId));

		List<PopulationGameSession> populationSessions =
			populationGameSessionRepository.findAllByGuestSessionKeyAndMemberIdIsNull(guestSessionKey);
		populationSessions.forEach(session -> session.claimOwnership(memberId));

		List<LeaderboardRecord> leaderboardRecords =
			leaderboardRecordRepository.findAllByGuestSessionKeyAndMemberIdIsNull(guestSessionKey);
		leaderboardRecords.forEach(record -> record.claimOwnership(memberId));
	}
}
