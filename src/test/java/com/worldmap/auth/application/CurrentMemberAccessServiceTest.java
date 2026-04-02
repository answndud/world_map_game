package com.worldmap.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.worldmap.auth.domain.Member;
import com.worldmap.auth.domain.MemberRepository;
import com.worldmap.auth.domain.MemberRole;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CurrentMemberAccessServiceTest {

	@Mock
	private MemberRepository memberRepository;

	private final MemberSessionManager memberSessionManager = new MemberSessionManager();

	@Test
	void currentMemberSynchronizesSessionWithPersistedMemberState() {
		CurrentMemberAccessService currentMemberAccessService = new CurrentMemberAccessService(
			memberSessionManager,
			memberRepository
		);
		MockHttpSession httpSession = new MockHttpSession();
		httpSession.setAttribute(MemberSessionManager.MEMBER_ID_ATTRIBUTE, 42L);
		httpSession.setAttribute(MemberSessionManager.MEMBER_NICKNAME_ATTRIBUTE, "old_runner");
		httpSession.setAttribute(MemberSessionManager.MEMBER_ROLE_ATTRIBUTE, MemberRole.USER.name());

		Member member = Member.create("orbit_runner", "hashed-password", MemberRole.ADMIN);
		ReflectionTestUtils.setField(member, "id", 42L);
		given(memberRepository.findById(42L)).willReturn(Optional.of(member));

		AuthenticatedMemberSession currentMember = currentMemberAccessService.currentMember(httpSession).orElseThrow();

		assertThat(currentMember.nickname()).isEqualTo("orbit_runner");
		assertThat(currentMember.role()).isEqualTo(MemberRole.ADMIN);
		assertThat(httpSession.getAttribute(MemberSessionManager.MEMBER_NICKNAME_ATTRIBUTE)).isEqualTo("orbit_runner");
		assertThat(httpSession.getAttribute(MemberSessionManager.MEMBER_ROLE_ATTRIBUTE)).isEqualTo(MemberRole.ADMIN.name());
	}

	@Test
	void currentMemberSignsOutDeletedMemberSession() {
		CurrentMemberAccessService currentMemberAccessService = new CurrentMemberAccessService(
			memberSessionManager,
			memberRepository
		);
		MockHttpSession httpSession = new MockHttpSession();
		httpSession.setAttribute(MemberSessionManager.MEMBER_ID_ATTRIBUTE, 42L);
		httpSession.setAttribute(MemberSessionManager.MEMBER_NICKNAME_ATTRIBUTE, "orbit_runner");
		httpSession.setAttribute(MemberSessionManager.MEMBER_ROLE_ATTRIBUTE, MemberRole.USER.name());

		given(memberRepository.findById(42L)).willReturn(Optional.empty());

		assertThat(currentMemberAccessService.currentMember(httpSession)).isEmpty();
		verify(memberRepository).findById(42L);
		assertThat(httpSession.getAttribute(MemberSessionManager.MEMBER_ID_ATTRIBUTE)).isNull();
		assertThat(httpSession.getAttribute(MemberSessionManager.MEMBER_NICKNAME_ATTRIBUTE)).isNull();
		assertThat(httpSession.getAttribute(MemberSessionManager.MEMBER_ROLE_ATTRIBUTE)).isNull();
	}

	@Test
	void currentMemberClearsMalformedSessionRole() {
		CurrentMemberAccessService currentMemberAccessService = new CurrentMemberAccessService(
			memberSessionManager,
			memberRepository
		);
		MockHttpSession httpSession = new MockHttpSession();
		httpSession.setAttribute(MemberSessionManager.MEMBER_ID_ATTRIBUTE, 42L);
		httpSession.setAttribute(MemberSessionManager.MEMBER_NICKNAME_ATTRIBUTE, "orbit_runner");
		httpSession.setAttribute(MemberSessionManager.MEMBER_ROLE_ATTRIBUTE, "BROKEN_ROLE");

		assertThat(currentMemberAccessService.currentMember(httpSession)).isEmpty();
		assertThat(httpSession.getAttribute(MemberSessionManager.MEMBER_ID_ATTRIBUTE)).isNull();
		assertThat(httpSession.getAttribute(MemberSessionManager.MEMBER_NICKNAME_ATTRIBUTE)).isNull();
		assertThat(httpSession.getAttribute(MemberSessionManager.MEMBER_ROLE_ATTRIBUTE)).isNull();
	}

	@Test
	void currentMemberCachesResolvedMemberPerRequest() {
		CurrentMemberAccessService currentMemberAccessService = new CurrentMemberAccessService(
			memberSessionManager,
			memberRepository
		);
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpSession httpSession = new MockHttpSession();
		request.setSession(httpSession);
		httpSession.setAttribute(MemberSessionManager.MEMBER_ID_ATTRIBUTE, 42L);
		httpSession.setAttribute(MemberSessionManager.MEMBER_NICKNAME_ATTRIBUTE, "old_runner");
		httpSession.setAttribute(MemberSessionManager.MEMBER_ROLE_ATTRIBUTE, MemberRole.USER.name());

		Member member = Member.create("orbit_runner", "hashed-password", MemberRole.ADMIN);
		ReflectionTestUtils.setField(member, "id", 42L);
		given(memberRepository.findById(42L)).willReturn(Optional.of(member));

		assertThat(currentMemberAccessService.currentMember(request)).contains(
			new AuthenticatedMemberSession(42L, "orbit_runner", MemberRole.ADMIN)
		);
		assertThat(currentMemberAccessService.currentMember(request)).contains(
			new AuthenticatedMemberSession(42L, "orbit_runner", MemberRole.ADMIN)
		);

		verify(memberRepository).findById(42L);
	}
}
