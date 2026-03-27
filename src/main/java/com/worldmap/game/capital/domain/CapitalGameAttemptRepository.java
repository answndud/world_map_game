package com.worldmap.game.capital.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CapitalGameAttemptRepository extends JpaRepository<CapitalGameAttempt, Long> {

	List<CapitalGameAttempt> findAllByStageSessionIdOrderByStageStageNumberAscAttemptNumberAsc(UUID sessionId);

	long countByStageSessionId(UUID sessionId);

	void deleteAllByStageSessionId(UUID sessionId);
}
