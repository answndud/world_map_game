package com.worldmap.game.common.application;

import com.worldmap.common.exception.SessionAccessDeniedException;
import com.worldmap.game.common.domain.BaseGameSession;

public record GameSessionAccessContext(
	Long memberId,
	String guestSessionKey
) {

	public static GameSessionAccessContext of(Long memberId, String guestSessionKey) {
		return new GameSessionAccessContext(memberId, guestSessionKey);
	}

	public static GameSessionAccessContext forMember(Long memberId) {
		return new GameSessionAccessContext(memberId, null);
	}

	public static GameSessionAccessContext forGuest(String guestSessionKey) {
		return new GameSessionAccessContext(null, guestSessionKey);
	}

	public static GameSessionAccessContext anonymous() {
		return new GameSessionAccessContext(null, null);
	}

	public void assertCanAccess(BaseGameSession session) {
		if (session == null) {
			throw new IllegalArgumentException("session은 비어 있을 수 없습니다.");
		}

		if (memberId != null && memberId.equals(session.getMemberId())) {
			return;
		}

		if (guestSessionKey != null && guestSessionKey.equals(session.getGuestSessionKey())) {
			return;
		}

		throw new SessionAccessDeniedException("현재 브라우저에서는 이 게임 세션에 접근할 수 없습니다.");
	}
}
