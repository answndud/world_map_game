package com.worldmap.auth.application;

import com.worldmap.game.common.application.GameSessionAccessContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

@Component
public class GameSessionAccessContextResolver {

	private final CurrentMemberAccessService currentMemberAccessService;
	private final GuestSessionKeyManager guestSessionKeyManager;

	public GameSessionAccessContextResolver(
		CurrentMemberAccessService currentMemberAccessService,
		GuestSessionKeyManager guestSessionKeyManager
	) {
		this.currentMemberAccessService = currentMemberAccessService;
		this.guestSessionKeyManager = guestSessionKeyManager;
	}

	public GameSessionAccessContext resolve(HttpServletRequest request) {
		HttpSession httpSession = request.getSession(false);
		if (httpSession == null) {
			return GameSessionAccessContext.anonymous();
		}

		Long memberId = currentMemberAccessService.currentMember(request)
			.map(AuthenticatedMemberSession::memberId)
			.orElse(null);
		String guestSessionKey = guestSessionKeyManager.currentGuestSessionKey(httpSession)
			.orElse(null);

		return GameSessionAccessContext.of(memberId, guestSessionKey);
	}
}
