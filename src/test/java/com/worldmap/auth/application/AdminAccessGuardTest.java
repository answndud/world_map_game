package com.worldmap.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.worldmap.auth.application.AdminAccessGuard.AdminAccessStatus;
import com.worldmap.auth.domain.Member;
import com.worldmap.auth.domain.MemberRepository;
import com.worldmap.auth.domain.MemberRole;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AdminAccessGuardTest {

	@Mock
	private MemberRepository memberRepository;

	private final MemberSessionManager memberSessionManager = new MemberSessionManager();

	@Test
	void authorizeUsesCurrentPersistedRoleAndSynchronizesSession() {
		AdminAccessGuard adminAccessGuard = new AdminAccessGuard(memberSessionManager, memberRepository);
		MockHttpSession httpSession = new MockHttpSession();
		httpSession.setAttribute(MemberSessionManager.MEMBER_ID_ATTRIBUTE, 42L);
		httpSession.setAttribute(MemberSessionManager.MEMBER_NICKNAME_ATTRIBUTE, "stale_admin");
		httpSession.setAttribute(MemberSessionManager.MEMBER_ROLE_ATTRIBUTE, MemberRole.ADMIN.name());

		Member member = Member.create("orbit_runner", "hashed-password", MemberRole.USER);
		ReflectionTestUtils.setField(member, "id", 42L);
		given(memberRepository.findById(42L)).willReturn(Optional.of(member));

		AdminAccessStatus accessStatus = adminAccessGuard.authorize(httpSession);

		assertThat(accessStatus).isEqualTo(AdminAccessStatus.FORBIDDEN);
		assertThat(httpSession.getAttribute(MemberSessionManager.MEMBER_NICKNAME_ATTRIBUTE)).isEqualTo("orbit_runner");
		assertThat(httpSession.getAttribute(MemberSessionManager.MEMBER_ROLE_ATTRIBUTE)).isEqualTo(MemberRole.USER.name());
	}

	@Test
	void authorizeSignsOutWhenPersistedMemberNoLongerExists() {
		AdminAccessGuard adminAccessGuard = new AdminAccessGuard(memberSessionManager, memberRepository);
		MockHttpSession httpSession = new MockHttpSession();
		httpSession.setAttribute(MemberSessionManager.MEMBER_ID_ATTRIBUTE, 42L);
		httpSession.setAttribute(MemberSessionManager.MEMBER_NICKNAME_ATTRIBUTE, "worldmap_admin");
		httpSession.setAttribute(MemberSessionManager.MEMBER_ROLE_ATTRIBUTE, MemberRole.ADMIN.name());

		given(memberRepository.findById(42L)).willReturn(Optional.empty());

		assertThat(adminAccessGuard.authorize(httpSession)).isEqualTo(AdminAccessStatus.UNAUTHENTICATED);
		verify(memberRepository).findById(42L);
		assertThat(httpSession.getAttribute(MemberSessionManager.MEMBER_ID_ATTRIBUTE)).isNull();
		assertThat(httpSession.getAttribute(MemberSessionManager.MEMBER_NICKNAME_ATTRIBUTE)).isNull();
		assertThat(httpSession.getAttribute(MemberSessionManager.MEMBER_ROLE_ATTRIBUTE)).isNull();
	}

	@Test
	void authorizeClearsMalformedSessionRoleInsteadOfFailing() {
		AdminAccessGuard adminAccessGuard = new AdminAccessGuard(memberSessionManager, memberRepository);
		MockHttpSession httpSession = new MockHttpSession();
		httpSession.setAttribute(MemberSessionManager.MEMBER_ID_ATTRIBUTE, 42L);
		httpSession.setAttribute(MemberSessionManager.MEMBER_NICKNAME_ATTRIBUTE, "worldmap_admin");
		httpSession.setAttribute(MemberSessionManager.MEMBER_ROLE_ATTRIBUTE, "BROKEN_ROLE");

		assertThat(adminAccessGuard.authorize(httpSession)).isEqualTo(AdminAccessStatus.UNAUTHENTICATED);
		assertThat(httpSession.getAttribute(MemberSessionManager.MEMBER_ID_ATTRIBUTE)).isNull();
		assertThat(httpSession.getAttribute(MemberSessionManager.MEMBER_NICKNAME_ATTRIBUTE)).isNull();
		assertThat(httpSession.getAttribute(MemberSessionManager.MEMBER_ROLE_ATTRIBUTE)).isNull();
	}
}
