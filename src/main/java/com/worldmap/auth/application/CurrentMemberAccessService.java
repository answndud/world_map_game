package com.worldmap.auth.application;

import com.worldmap.auth.domain.Member;
import com.worldmap.auth.domain.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class CurrentMemberAccessService {

	private static final String CURRENT_MEMBER_REQUEST_ATTRIBUTE =
		CurrentMemberAccessService.class.getName() + ".CURRENT_MEMBER";
	private static final Object NO_CURRENT_MEMBER = new Object();

	private final MemberSessionManager memberSessionManager;
	private final MemberRepository memberRepository;

	public CurrentMemberAccessService(
		MemberSessionManager memberSessionManager,
		MemberRepository memberRepository
	) {
		this.memberSessionManager = memberSessionManager;
		this.memberRepository = memberRepository;
	}

	public Optional<AuthenticatedMemberSession> currentMember(HttpServletRequest request) {
		if (request == null) {
			return Optional.empty();
		}

		Object cachedCurrentMember = request.getAttribute(CURRENT_MEMBER_REQUEST_ATTRIBUTE);
		if (cachedCurrentMember == NO_CURRENT_MEMBER) {
			return Optional.empty();
		}
		if (cachedCurrentMember instanceof AuthenticatedMemberSession currentMember) {
			return Optional.of(currentMember);
		}

		Optional<AuthenticatedMemberSession> resolvedCurrentMember = currentMember(request.getSession(false));
		request.setAttribute(CURRENT_MEMBER_REQUEST_ATTRIBUTE, resolvedCurrentMember.<Object>map(member -> member).orElse(NO_CURRENT_MEMBER));
		return resolvedCurrentMember;
	}

	public Optional<AuthenticatedMemberSession> currentMember(HttpSession httpSession) {
		if (httpSession == null) {
			return Optional.empty();
		}

		Optional<AuthenticatedMemberSession> sessionMember = sessionMember(httpSession);
		if (sessionMember.isEmpty()) {
			return Optional.empty();
		}

		Optional<Member> persistedMember = memberRepository.findById(sessionMember.get().memberId());
		if (persistedMember.isEmpty()) {
			memberSessionManager.signOut(httpSession);
			return Optional.empty();
		}

		Member member = persistedMember.get();
		memberSessionManager.syncMember(httpSession, member);
		return Optional.of(new AuthenticatedMemberSession(
			member.getId(),
			member.getNickname(),
			member.getRole()
		));
	}

	private Optional<AuthenticatedMemberSession> sessionMember(HttpSession httpSession) {
		try {
			return memberSessionManager.currentMember(httpSession);
		} catch (IllegalArgumentException ex) {
			memberSessionManager.signOut(httpSession);
			return Optional.empty();
		}
	}
}
