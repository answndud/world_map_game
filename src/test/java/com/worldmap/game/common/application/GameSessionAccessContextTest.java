package com.worldmap.game.common.application;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.worldmap.common.exception.SessionAccessDeniedException;
import com.worldmap.game.location.domain.LocationGameSession;
import org.junit.jupiter.api.Test;

class GameSessionAccessContextTest {

	@Test
	void memberContextAllowsOwnedMemberSession() {
		LocationGameSession session = LocationGameSession.ready("member-player", 42L, null, 1);

		assertThatCode(() -> new GameSessionAccessContext(42L, null).assertCanAccess(session))
			.doesNotThrowAnyException();
	}

	@Test
	void guestContextAllowsOwnedGuestSession() {
		LocationGameSession session = LocationGameSession.ready("guest-player", null, "guest-123", 1);

		assertThatCode(() -> new GameSessionAccessContext(null, "guest-123").assertCanAccess(session))
			.doesNotThrowAnyException();
	}

	@Test
	void mismatchedContextIsRejected() {
		LocationGameSession session = LocationGameSession.ready("guest-player", null, "guest-owner", 1);

		assertThatThrownBy(() -> new GameSessionAccessContext(null, "guest-intruder").assertCanAccess(session))
			.isInstanceOf(SessionAccessDeniedException.class)
			.hasMessageContaining("접근");
	}
}
