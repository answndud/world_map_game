package com.worldmap.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.worldmap.auth.domain.MemberRole;
import com.worldmap.game.common.application.GameSessionAccessContext;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;

@ExtendWith(MockitoExtension.class)
class GameSessionAccessContextResolverTest {

	@Mock
	private CurrentMemberAccessService currentMemberAccessService;

	private final GuestSessionKeyManager guestSessionKeyManager = new GuestSessionKeyManager();

	@Test
	void resolveUsesAuthenticatedMemberWhenPresent() {
		GameSessionAccessContextResolver resolver =
			new GameSessionAccessContextResolver(currentMemberAccessService, guestSessionKeyManager);
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpSession session = new MockHttpSession();
		request.setSession(session);
		given(currentMemberAccessService.currentMember(session)).willReturn(Optional.of(
			new AuthenticatedMemberSession(42L, "member", MemberRole.USER)
		));

		GameSessionAccessContext context = resolver.resolve(request);

		assertThat(context.memberId()).isEqualTo(42L);
		assertThat(context.guestSessionKey()).isNull();
	}

	@Test
	void resolveUsesGuestSessionKeyWhenAnonymousGuestHasSession() {
		GameSessionAccessContextResolver resolver =
			new GameSessionAccessContextResolver(currentMemberAccessService, guestSessionKeyManager);
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpSession session = new MockHttpSession();
		request.setSession(session);
		String guestSessionKey = guestSessionKeyManager.ensureGuestSessionKey(session);
		given(currentMemberAccessService.currentMember(session)).willReturn(Optional.empty());

		GameSessionAccessContext context = resolver.resolve(request);

		assertThat(context.memberId()).isNull();
		assertThat(context.guestSessionKey()).isEqualTo(guestSessionKey);
	}

	@Test
	void resolveReturnsAnonymousContextWhenNoSessionExists() {
		GameSessionAccessContextResolver resolver =
			new GameSessionAccessContextResolver(currentMemberAccessService, guestSessionKeyManager);
		GameSessionAccessContext context = resolver.resolve(new MockHttpServletRequest());

		assertThat(context.memberId()).isNull();
		assertThat(context.guestSessionKey()).isNull();
	}
}
