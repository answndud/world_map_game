package com.worldmap.auth.application;

import com.worldmap.auth.domain.Member;
import com.worldmap.auth.domain.MemberRepository;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class CurrentMemberAccessService {

	private final MemberSessionManager memberSessionManager;
	private final MemberRepository memberRepository;

	public CurrentMemberAccessService(
		MemberSessionManager memberSessionManager,
		MemberRepository memberRepository
	) {
		this.memberSessionManager = memberSessionManager;
		this.memberRepository = memberRepository;
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
