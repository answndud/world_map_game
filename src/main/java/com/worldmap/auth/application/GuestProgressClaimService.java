package com.worldmap.auth.application;

import com.worldmap.game.capital.domain.CapitalGameSession;
import com.worldmap.game.capital.domain.CapitalGameSessionRepository;
import com.worldmap.game.flag.domain.FlagGameSession;
import com.worldmap.game.flag.domain.FlagGameSessionRepository;
import com.worldmap.game.location.domain.LocationGameSession;
import com.worldmap.game.location.domain.LocationGameSessionRepository;
import com.worldmap.game.population.domain.PopulationGameSession;
import com.worldmap.game.population.domain.PopulationGameSessionRepository;
import com.worldmap.game.populationbattle.domain.PopulationBattleGameSession;
import com.worldmap.game.populationbattle.domain.PopulationBattleGameSessionRepository;
import com.worldmap.ranking.domain.LeaderboardRecord;
import com.worldmap.ranking.domain.LeaderboardRecordRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GuestProgressClaimService {

	private final LocationGameSessionRepository locationGameSessionRepository;
	private final PopulationGameSessionRepository populationGameSessionRepository;
	private final CapitalGameSessionRepository capitalGameSessionRepository;
	private final FlagGameSessionRepository flagGameSessionRepository;
	private final PopulationBattleGameSessionRepository populationBattleGameSessionRepository;
	private final LeaderboardRecordRepository leaderboardRecordRepository;

	public GuestProgressClaimService(
		LocationGameSessionRepository locationGameSessionRepository,
		PopulationGameSessionRepository populationGameSessionRepository,
		CapitalGameSessionRepository capitalGameSessionRepository,
		FlagGameSessionRepository flagGameSessionRepository,
		PopulationBattleGameSessionRepository populationBattleGameSessionRepository,
		LeaderboardRecordRepository leaderboardRecordRepository
	) {
		this.locationGameSessionRepository = locationGameSessionRepository;
		this.populationGameSessionRepository = populationGameSessionRepository;
		this.capitalGameSessionRepository = capitalGameSessionRepository;
		this.flagGameSessionRepository = flagGameSessionRepository;
		this.populationBattleGameSessionRepository = populationBattleGameSessionRepository;
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

		List<CapitalGameSession> capitalSessions =
			capitalGameSessionRepository.findAllByGuestSessionKeyAndMemberIdIsNull(guestSessionKey);
		capitalSessions.forEach(session -> session.claimOwnership(memberId));

		List<FlagGameSession> flagSessions =
			flagGameSessionRepository.findAllByGuestSessionKeyAndMemberIdIsNull(guestSessionKey);
		flagSessions.forEach(session -> session.claimOwnership(memberId));

		List<PopulationBattleGameSession> populationBattleSessions =
			populationBattleGameSessionRepository.findAllByGuestSessionKeyAndMemberIdIsNull(guestSessionKey);
		populationBattleSessions.forEach(session -> session.claimOwnership(memberId));

		List<LeaderboardRecord> leaderboardRecords =
			leaderboardRecordRepository.findAllByGuestSessionKeyAndMemberIdIsNull(guestSessionKey);
		leaderboardRecords.forEach(record -> record.claimOwnership(memberId));
	}
}
