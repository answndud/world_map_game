package com.worldmap.auth.application;

import com.worldmap.auth.domain.MemberRole;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

@Component
public class AdminAccessGuard {

	private final CurrentMemberAccessService currentMemberAccessService;

	public AdminAccessGuard(CurrentMemberAccessService currentMemberAccessService) {
		this.currentMemberAccessService = currentMemberAccessService;
	}

	public AdminAccessStatus authorize(HttpSession httpSession) {
		if (httpSession == null) {
			return AdminAccessStatus.UNAUTHENTICATED;
		}

		var currentMember = currentMemberAccessService.currentMember(httpSession);
		if (currentMember.isEmpty()) {
			return AdminAccessStatus.UNAUTHENTICATED;
		}
		if (currentMember.get().role() != MemberRole.ADMIN) {
			return AdminAccessStatus.FORBIDDEN;
		}

		return AdminAccessStatus.ALLOWED;
	}

	public AdminAccessStatus authorize(HttpServletRequest request) {
		if (request == null) {
			return AdminAccessStatus.UNAUTHENTICATED;
		}

		var currentMember = currentMemberAccessService.currentMember(request);
		if (currentMember.isEmpty()) {
			return AdminAccessStatus.UNAUTHENTICATED;
		}
		if (currentMember.get().role() != MemberRole.ADMIN) {
			return AdminAccessStatus.FORBIDDEN;
		}

		return AdminAccessStatus.ALLOWED;
	}

	public enum AdminAccessStatus {
		ALLOWED,
		UNAUTHENTICATED,
		FORBIDDEN
	}
}
