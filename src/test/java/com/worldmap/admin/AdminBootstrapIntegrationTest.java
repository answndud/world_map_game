package com.worldmap.admin;

import static org.assertj.core.api.Assertions.assertThat;

import com.worldmap.auth.application.MemberPasswordHasher;
import com.worldmap.auth.domain.Member;
import com.worldmap.auth.domain.MemberRepository;
import com.worldmap.auth.domain.MemberRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
	properties = {
		"worldmap.admin.bootstrap.enabled=true",
		"worldmap.admin.bootstrap.nickname=worldmap_admin",
		"worldmap.admin.bootstrap.password=secret123"
	}
)
@ActiveProfiles("test")
class AdminBootstrapIntegrationTest {

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private MemberPasswordHasher memberPasswordHasher;

	@Test
	void startupBootstrapCreatesAdminMemberFromConfiguredCredentials() {
		Member adminMember = memberRepository.findByNicknameIgnoreCase("worldmap_admin").orElseThrow();
		assertThat(adminMember.getRole()).isEqualTo(MemberRole.ADMIN);
		assertThat(memberPasswordHasher.matches("secret123", adminMember.getPasswordHash())).isTrue();
	}
}
