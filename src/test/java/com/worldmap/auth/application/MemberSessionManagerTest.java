package com.worldmap.auth.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.worldmap.auth.domain.Member;
import com.worldmap.auth.domain.MemberRole;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

class MemberSessionManagerTest {

	private final MemberSessionManager memberSessionManager = new MemberSessionManager();

	@Test
	void signInRotatesSessionIdAndStoresMemberAttributes() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.getSession();
		String previousSessionId = request.getSession().getId();
		Member member = Member.create("orbit_runner", "hashed-password", MemberRole.USER);
		ReflectionTestUtils.setField(member, "id", 42L);

		memberSessionManager.signIn(request, member);

		assertThat(request.getSession().getId()).isNotEqualTo(previousSessionId);
		assertThat(request.getSession().getAttribute(MemberSessionManager.MEMBER_ID_ATTRIBUTE)).isEqualTo(42L);
		assertThat(request.getSession().getAttribute(MemberSessionManager.MEMBER_NICKNAME_ATTRIBUTE)).isEqualTo("orbit_runner");
		assertThat(request.getSession().getAttribute(MemberSessionManager.MEMBER_ROLE_ATTRIBUTE)).isEqualTo(MemberRole.USER.name());
	}
}
