package com.worldmap.admin.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.worldmap.auth.application.MemberCredentialPolicy;
import com.worldmap.auth.application.MemberPasswordHasher;
import com.worldmap.auth.domain.Member;
import com.worldmap.auth.domain.MemberRepository;
import com.worldmap.auth.domain.MemberRole;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminBootstrapServiceTest {

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private MemberPasswordHasher memberPasswordHasher;

	private final MemberCredentialPolicy memberCredentialPolicy = new MemberCredentialPolicy();

	@Test
	void ensureBootstrapAdminCreatesAdminWhenMissing() {
		AdminBootstrapProperties properties = new AdminBootstrapProperties();
		properties.setEnabled(true);
		properties.setNickname("worldmap_admin");
		properties.setPassword("secret123");

		AdminBootstrapService adminBootstrapService = new AdminBootstrapService(
			properties,
			memberRepository,
			memberPasswordHasher,
			memberCredentialPolicy
		);

		given(memberRepository.findByNicknameIgnoreCase("worldmap_admin")).willReturn(Optional.empty());
		given(memberPasswordHasher.hash("secret123")).willReturn("hashed-admin-password");

		adminBootstrapService.ensureBootstrapAdmin();

		ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
		verify(memberRepository).save(memberCaptor.capture());
		Member savedMember = memberCaptor.getValue();
		assertThat(savedMember.getNickname()).isEqualTo("worldmap_admin");
		assertThat(savedMember.getRole()).isEqualTo(MemberRole.ADMIN);
		assertThat(savedMember.getPasswordHash()).isEqualTo("hashed-admin-password");
	}

	@Test
	void ensureBootstrapAdminPromotesExistingMemberToAdmin() {
		AdminBootstrapProperties properties = new AdminBootstrapProperties();
		properties.setEnabled(true);
		properties.setNickname("orbit_runner");
		properties.setPassword("secret123");

		AdminBootstrapService adminBootstrapService = new AdminBootstrapService(
			properties,
			memberRepository,
			memberPasswordHasher,
			memberCredentialPolicy
		);

		Member existingMember = Member.create("orbit_runner", "old-hash", MemberRole.USER);
		given(memberRepository.findByNicknameIgnoreCase("orbit_runner")).willReturn(Optional.of(existingMember));
		given(memberPasswordHasher.hash("secret123")).willReturn("new-hash");

		adminBootstrapService.ensureBootstrapAdmin();

		assertThat(existingMember.getRole()).isEqualTo(MemberRole.ADMIN);
		assertThat(existingMember.getPasswordHash()).isEqualTo("new-hash");
		verify(memberRepository, never()).save(any(Member.class));
	}

	@Test
	void ensureBootstrapAdminFailsFastWhenEnabledWithoutPassword() {
		AdminBootstrapProperties properties = new AdminBootstrapProperties();
		properties.setEnabled(true);
		properties.setNickname("worldmap_admin");
		properties.setPassword("");

		AdminBootstrapService adminBootstrapService = new AdminBootstrapService(
			properties,
			memberRepository,
			memberPasswordHasher,
			memberCredentialPolicy
		);

		assertThatThrownBy(adminBootstrapService::ensureBootstrapAdmin)
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("비밀번호를 입력해주세요.");
	}
}
