package com.worldmap.auth.application;

import com.worldmap.auth.domain.Member;
import com.worldmap.auth.domain.MemberRepository;
import com.worldmap.auth.domain.MemberRole;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class AdminAccessGuard {

	private final MemberSessionManager memberSessionManager;
	private final MemberRepository memberRepository;

	public AdminAccessGuard(
		MemberSessionManager memberSessionManager,
		MemberRepository memberRepository
	) {
		this.memberSessionManager = memberSessionManager;
		this.memberRepository = memberRepository;
	}

	public AdminAccessStatus authorize(HttpSession httpSession) {
		if (httpSession == null) {
			return AdminAccessStatus.UNAUTHENTICATED;
		}

		Optional<AuthenticatedMemberSession> currentMember = currentMember(httpSession);
		if (currentMember.isEmpty()) {
			return AdminAccessStatus.UNAUTHENTICATED;
		}

		Optional<Member> persistedMember = memberRepository.findById(currentMember.get().memberId());
		if (persistedMember.isEmpty()) {
			memberSessionManager.signOut(httpSession);
			return AdminAccessStatus.UNAUTHENTICATED;
		}

		memberSessionManager.syncMember(httpSession, persistedMember.get());
		if (persistedMember.get().getRole() != MemberRole.ADMIN) {
			return AdminAccessStatus.FORBIDDEN;
		}

		return AdminAccessStatus.ALLOWED;
	}

	private Optional<AuthenticatedMemberSession> currentMember(HttpSession httpSession) {
		try {
			return memberSessionManager.currentMember(httpSession);
		} catch (IllegalArgumentException ex) {
			memberSessionManager.signOut(httpSession);
			return Optional.empty();
		}
	}

	public enum AdminAccessStatus {
		ALLOWED,
		UNAUTHENTICATED,
		FORBIDDEN
	}
}
