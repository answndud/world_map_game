package com.worldmap.auth.application;

import jakarta.servlet.http.HttpSession;
import java.util.UUID;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class GuestSessionKeyManager {

	static final String GUEST_SESSION_KEY_ATTRIBUTE = "WORLDMAP_GUEST_SESSION_KEY";

	public String ensureGuestSessionKey(HttpSession httpSession) {
		Object existingValue = httpSession.getAttribute(GUEST_SESSION_KEY_ATTRIBUTE);
		if (existingValue instanceof String guestSessionKey && !guestSessionKey.isBlank()) {
			return guestSessionKey;
		}

		String guestSessionKey = "guest-" + UUID.randomUUID();
		httpSession.setAttribute(GUEST_SESSION_KEY_ATTRIBUTE, guestSessionKey);
		return guestSessionKey;
	}

	public Optional<String> currentGuestSessionKey(HttpSession httpSession) {
		if (httpSession == null) {
			return Optional.empty();
		}

		Object existingValue = httpSession.getAttribute(GUEST_SESSION_KEY_ATTRIBUTE);
		if (existingValue instanceof String guestSessionKey && !guestSessionKey.isBlank()) {
			return Optional.of(guestSessionKey);
		}
		return Optional.empty();
	}

	public String rotateGuestSessionKey(HttpSession httpSession) {
		String guestSessionKey = "guest-" + UUID.randomUUID();
		httpSession.setAttribute(GUEST_SESSION_KEY_ATTRIBUTE, guestSessionKey);
		return guestSessionKey;
	}
}
