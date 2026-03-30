package com.worldmap.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.worldmap.auth.application.AdminAccessGuard.AdminAccessStatus;
import com.worldmap.auth.domain.MemberRole;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;

@ExtendWith(MockitoExtension.class)
class AdminAccessGuardTest {

	@Mock
	private CurrentMemberAccessService currentMemberAccessService;

	@Test
	void authorizeAllowsCurrentAdminMember() {
		AdminAccessGuard adminAccessGuard = new AdminAccessGuard(currentMemberAccessService);
		MockHttpSession httpSession = new MockHttpSession();
		given(currentMemberAccessService.currentMember(httpSession)).willReturn(Optional.of(
			new AuthenticatedMemberSession(42L, "worldmap_admin", MemberRole.ADMIN)
		));

		assertThat(adminAccessGuard.authorize(httpSession)).isEqualTo(AdminAccessStatus.ALLOWED);
	}

	@Test
	void authorizeRejectsCurrentNonAdminMember() {
		AdminAccessGuard adminAccessGuard = new AdminAccessGuard(currentMemberAccessService);
		MockHttpSession httpSession = new MockHttpSession();
		given(currentMemberAccessService.currentMember(httpSession)).willReturn(Optional.of(
			new AuthenticatedMemberSession(42L, "orbit_runner", MemberRole.USER)
		));

		assertThat(adminAccessGuard.authorize(httpSession)).isEqualTo(AdminAccessStatus.FORBIDDEN);
	}

	@Test
	void authorizeTreatsMissingCurrentMemberAsUnauthenticated() {
		AdminAccessGuard adminAccessGuard = new AdminAccessGuard(currentMemberAccessService);
		MockHttpSession httpSession = new MockHttpSession();
		given(currentMemberAccessService.currentMember(httpSession)).willReturn(Optional.empty());

		assertThat(adminAccessGuard.authorize(httpSession)).isEqualTo(AdminAccessStatus.UNAUTHENTICATED);
	}

	@Test
	void authorizeRequestReusesCurrentMemberFromRequestScope() {
		AdminAccessGuard adminAccessGuard = new AdminAccessGuard(currentMemberAccessService);
		MockHttpServletRequest request = new MockHttpServletRequest();
		given(currentMemberAccessService.currentMember(request)).willReturn(Optional.of(
			new AuthenticatedMemberSession(42L, "worldmap_admin", MemberRole.ADMIN)
		));

		assertThat(adminAccessGuard.authorize(request)).isEqualTo(AdminAccessStatus.ALLOWED);
	}
}
