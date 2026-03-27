package com.worldmap.game.flag.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlagGameAttemptRepository extends JpaRepository<FlagGameAttempt, Long> {

	List<FlagGameAttempt> findAllByStageSessionIdOrderByStageStageNumberAscAttemptNumberAsc(UUID sessionId);

	long countByStageSessionId(UUID sessionId);

	void deleteAllByStageSessionId(UUID sessionId);
}
