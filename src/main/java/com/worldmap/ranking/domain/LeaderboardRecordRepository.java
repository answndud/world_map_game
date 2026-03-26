package com.worldmap.ranking.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaderboardRecordRepository extends JpaRepository<LeaderboardRecord, Long> {

	Optional<LeaderboardRecord> findByRunSignature(String runSignature);

	List<LeaderboardRecord> findAllByGuestSessionKeyAndMemberIdIsNull(String guestSessionKey);

	long countByMemberId(Long memberId);

	long countByMemberIdAndGameMode(
		Long memberId,
		LeaderboardGameMode gameMode
	);

	Page<LeaderboardRecord> findByMemberIdOrderByFinishedAtDesc(Long memberId, Pageable pageable);

	long countByFinishedAtGreaterThanEqualAndFinishedAtLessThan(LocalDateTime startInclusive, LocalDateTime endExclusive);

	long countByGameModeAndFinishedAtGreaterThanEqualAndFinishedAtLessThan(
		LeaderboardGameMode gameMode,
		LocalDateTime startInclusive,
		LocalDateTime endExclusive
	);

	Optional<LeaderboardRecord> findFirstByMemberIdAndGameModeOrderByRankingScoreDescFinishedAtAsc(
		Long memberId,
		LeaderboardGameMode gameMode
	);

	List<LeaderboardRecord> findAllByGameModeOrderByRankingScoreDescFinishedAtAsc(LeaderboardGameMode gameMode);

	Page<LeaderboardRecord> findByGameModeOrderByRankingScoreDescFinishedAtAsc(LeaderboardGameMode gameMode, Pageable pageable);

	Page<LeaderboardRecord> findByGameModeAndLeaderboardDateOrderByRankingScoreDescFinishedAtAsc(
		LeaderboardGameMode gameMode,
		LocalDate leaderboardDate,
		Pageable pageable
	);
}
