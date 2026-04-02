package com.worldmap.auth.application;

import com.worldmap.auth.domain.Member;
import com.worldmap.auth.domain.MemberRole;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class MemberSessionManager {

	public static final String MEMBER_ID_ATTRIBUTE = "WORLDMAP_MEMBER_ID";
	public static final String MEMBER_NICKNAME_ATTRIBUTE = "WORLDMAP_MEMBER_NICKNAME";
	public static final String MEMBER_ROLE_ATTRIBUTE = "WORLDMAP_MEMBER_ROLE";

	public void signIn(HttpServletRequest request, Member member) {
		request.changeSessionId();
		HttpSession httpSession = request.getSession();
		syncMember(httpSession, member);
	}

	public void syncMember(HttpSession httpSession, Member member) {
		httpSession.setAttribute(MEMBER_ID_ATTRIBUTE, member.getId());
		httpSession.setAttribute(MEMBER_NICKNAME_ATTRIBUTE, member.getNickname());
		httpSession.setAttribute(MEMBER_ROLE_ATTRIBUTE, member.getRole().name());
	}

	public void signOut(HttpSession httpSession) {
		httpSession.removeAttribute(MEMBER_ID_ATTRIBUTE);
		httpSession.removeAttribute(MEMBER_NICKNAME_ATTRIBUTE);
		httpSession.removeAttribute(MEMBER_ROLE_ATTRIBUTE);
	}

	public Optional<AuthenticatedMemberSession> currentMember(HttpSession httpSession) {
		if (httpSession == null) {
			return Optional.empty();
		}

		Object memberId = httpSession.getAttribute(MEMBER_ID_ATTRIBUTE);
		Object nickname = httpSession.getAttribute(MEMBER_NICKNAME_ATTRIBUTE);
		Object role = httpSession.getAttribute(MEMBER_ROLE_ATTRIBUTE);

		if (!(memberId instanceof Long authenticatedMemberId)) {
			return Optional.empty();
		}
		if (!(nickname instanceof String authenticatedNickname) || authenticatedNickname.isBlank()) {
			return Optional.empty();
		}
		if (!(role instanceof String memberRoleName) || memberRoleName.isBlank()) {
			return Optional.empty();
		}

		return Optional.of(
			new AuthenticatedMemberSession(
				authenticatedMemberId,
				authenticatedNickname,
				MemberRole.valueOf(memberRoleName)
			)
		);
	}
}
