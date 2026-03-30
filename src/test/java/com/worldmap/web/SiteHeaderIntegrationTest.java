package com.worldmap.web;

import static com.worldmap.auth.application.MemberSessionManager.MEMBER_ID_ATTRIBUTE;
import static com.worldmap.auth.application.MemberSessionManager.MEMBER_NICKNAME_ATTRIBUTE;
import static com.worldmap.auth.application.MemberSessionManager.MEMBER_ROLE_ATTRIBUTE;
import static com.worldmap.auth.domain.MemberRole.ADMIN;
import static com.worldmap.auth.domain.MemberRole.USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.worldmap.auth.domain.Member;
import com.worldmap.auth.domain.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SiteHeaderIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private MemberRepository memberRepository;

	@BeforeEach
	void setUp() {
		memberRepository.deleteAll();
	}

	@Test
	void siteHeaderHidesDashboardLinkAfterAdminRoleIsRevoked() throws Exception {
		Member member = memberRepository.save(Member.create("worldmap_admin", "hash", ADMIN));
		MockHttpSession session = sessionFor(member);

		mockMvc.perform(get("/").session(session))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString(">Dashboard<")));

		member.provisionUser("hash");
		memberRepository.save(member);

		mockMvc.perform(get("/").session(session))
			.andExpect(status().isOk())
			.andExpect(content().string(not(containsString(">Dashboard<"))));

		assertThat(session.getAttribute(MEMBER_ROLE_ATTRIBUTE)).isEqualTo(USER.name());
	}

	@Test
	void siteHeaderShowsDashboardLinkWhenCurrentRoleIsPromotedToAdmin() throws Exception {
		Member member = memberRepository.save(Member.create("orbit_runner", "hash", USER));
		MockHttpSession session = sessionFor(member);

		mockMvc.perform(get("/").session(session))
			.andExpect(status().isOk())
			.andExpect(content().string(not(containsString(">Dashboard<"))));

		member.provisionAdmin("hash");
		memberRepository.save(member);

		mockMvc.perform(get("/").session(session))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString(">Dashboard<")));

		assertThat(session.getAttribute(MEMBER_ROLE_ATTRIBUTE)).isEqualTo(ADMIN.name());
	}

	private MockHttpSession sessionFor(Member member) {
		MockHttpSession session = new MockHttpSession();
		session.setAttribute(MEMBER_ID_ATTRIBUTE, member.getId());
		session.setAttribute(MEMBER_NICKNAME_ATTRIBUTE, member.getNickname());
		session.setAttribute(MEMBER_ROLE_ATTRIBUTE, member.getRole().name());
		return session;
	}
}
