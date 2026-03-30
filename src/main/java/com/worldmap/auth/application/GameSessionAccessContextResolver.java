package com.worldmap.auth.application;

import com.worldmap.game.common.application.GameSessionAccessContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

@Component
public class GameSessionAccessContextResolver {

	private final MemberSessionManager memberSessionManager;
	private final GuestSessionKeyManager guestSessionKeyManager;

	public GameSessionAccessContextResolver(
		MemberSessionManager memberSessionManager,
		GuestSessionKeyManager guestSessionKeyManager
	) {
		this.memberSessionManager = memberSessionManager;
		this.guestSessionKeyManager = guestSessionKeyManager;
	}

	public GameSessionAccessContext resolve(HttpServletRequest request) {
		HttpSession httpSession = request.getSession(false);
		if (httpSession == null) {
			return GameSessionAccessContext.anonymous();
		}

		Long memberId = memberSessionManager.currentMember(httpSession)
			.map(AuthenticatedMemberSession::memberId)
			.orElse(null);
		String guestSessionKey = guestSessionKeyManager.currentGuestSessionKey(httpSession)
			.orElse(null);

		return GameSessionAccessContext.of(memberId, guestSessionKey);
	}
}
