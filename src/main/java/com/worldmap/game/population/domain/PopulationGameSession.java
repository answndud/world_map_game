package com.worldmap.game.population.domain;

import com.worldmap.game.common.domain.BaseGameSession;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "population_game_session")
public class PopulationGameSession extends BaseGameSession {

	protected PopulationGameSession() {
	}

	private PopulationGameSession(UUID id, String playerNickname, Integer totalRounds) {
		super(id, playerNickname, totalRounds);
	}

	public static PopulationGameSession ready(String playerNickname, Integer totalRounds) {
		return new PopulationGameSession(UUID.randomUUID(), playerNickname, totalRounds);
	}
}
