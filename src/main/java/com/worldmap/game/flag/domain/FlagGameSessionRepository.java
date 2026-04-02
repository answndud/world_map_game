package com.worldmap.game.flag.domain;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FlagGameSessionRepository extends JpaRepository<FlagGameSession, UUID> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select session from FlagGameSession session where session.id = :sessionId")
	Optional<FlagGameSession> findByIdForUpdate(@Param("sessionId") UUID sessionId);

	List<FlagGameSession> findAllByGuestSessionKeyAndMemberIdIsNull(String guestSessionKey);

	long countByMemberIdAndFinishedAtIsNotNull(Long memberId);

	long countByStartedAtGreaterThanEqualAndStartedAtLessThan(LocalDateTime startInclusive, LocalDateTime endExclusive);

	@Query("""
		select distinct session.memberId
		from FlagGameSession session
		where session.startedAt >= :startInclusive
		  and session.startedAt < :endExclusive
		  and session.memberId is not null
		""")
	List<Long> findDistinctMemberIdsByStartedAtBetween(
		@Param("startInclusive") LocalDateTime startInclusive,
		@Param("endExclusive") LocalDateTime endExclusive
	);

	@Query("""
		select distinct session.guestSessionKey
		from FlagGameSession session
		where session.startedAt >= :startInclusive
		  and session.startedAt < :endExclusive
		  and session.guestSessionKey is not null
		""")
	List<String> findDistinctGuestSessionKeysByStartedAtBetween(
		@Param("startInclusive") LocalDateTime startInclusive,
		@Param("endExclusive") LocalDateTime endExclusive
	);
}
