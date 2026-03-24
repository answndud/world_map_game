package com.worldmap.admin.application;

import com.worldmap.auth.application.MemberCredentialPolicy;
import com.worldmap.auth.application.MemberPasswordHasher;
import com.worldmap.auth.domain.Member;
import com.worldmap.auth.domain.MemberRepository;
import com.worldmap.auth.domain.MemberRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminBootstrapService {

	private static final Logger log = LoggerFactory.getLogger(AdminBootstrapService.class);

	private final AdminBootstrapProperties adminBootstrapProperties;
	private final MemberRepository memberRepository;
	private final MemberPasswordHasher memberPasswordHasher;
	private final MemberCredentialPolicy memberCredentialPolicy;

	public AdminBootstrapService(
		AdminBootstrapProperties adminBootstrapProperties,
		MemberRepository memberRepository,
		MemberPasswordHasher memberPasswordHasher,
		MemberCredentialPolicy memberCredentialPolicy
	) {
		this.adminBootstrapProperties = adminBootstrapProperties;
		this.memberRepository = memberRepository;
		this.memberPasswordHasher = memberPasswordHasher;
		this.memberCredentialPolicy = memberCredentialPolicy;
	}

	@Transactional
	public void ensureBootstrapAdmin() {
		if (!adminBootstrapProperties.isEnabled()) {
			return;
		}

		String nickname = memberCredentialPolicy.normalizeNickname(adminBootstrapProperties.getNickname());
		memberCredentialPolicy.validatePassword(adminBootstrapProperties.getPassword());
		String passwordHash = memberPasswordHasher.hash(adminBootstrapProperties.getPassword());

		memberRepository.findByNicknameIgnoreCase(nickname)
			.ifPresentOrElse(existingMember -> {
				existingMember.provisionAdmin(passwordHash);
				log.info("Provisioned existing member [{}] as bootstrap admin.", nickname);
			}, () -> {
				memberRepository.save(Member.create(nickname, passwordHash, MemberRole.ADMIN));
				log.info("Created bootstrap admin member [{}].", nickname);
			});
	}
}
