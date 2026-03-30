package com.worldmap.auth.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.worldmap.auth.domain.Member;
import com.worldmap.auth.domain.MemberRole;
import com.worldmap.game.common.application.GameSessionAccessContext;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

class GameSessionAccessContextResolverTest {

	private final MemberSessionManager memberSessionManager = new MemberSessionManager();
	private final GuestSessionKeyManager guestSessionKeyManager = new GuestSessionKeyManager();
	private final GameSessionAccessContextResolver resolver =
		new GameSessionAccessContextResolver(memberSessionManager, guestSessionKeyManager);

	@Test
	void resolveUsesAuthenticatedMemberWhenPresent() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.getSession();
		Member member = Member.create("member", "hashed", MemberRole.USER);
		ReflectionTestUtils.setField(member, "id", 42L);
		memberSessionManager.signIn(request, member);

		GameSessionAccessContext context = resolver.resolve(request);

		assertThat(context.memberId()).isEqualTo(42L);
		assertThat(context.guestSessionKey()).isNull();
	}

	@Test
	void resolveUsesGuestSessionKeyWhenAnonymousGuestHasSession() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		String guestSessionKey = guestSessionKeyManager.ensureGuestSessionKey(request.getSession());

		GameSessionAccessContext context = resolver.resolve(request);

		assertThat(context.memberId()).isNull();
		assertThat(context.guestSessionKey()).isEqualTo(guestSessionKey);
	}

	@Test
	void resolveReturnsAnonymousContextWhenNoSessionExists() {
		GameSessionAccessContext context = resolver.resolve(new MockHttpServletRequest());

		assertThat(context.memberId()).isNull();
		assertThat(context.guestSessionKey()).isNull();
	}
}
