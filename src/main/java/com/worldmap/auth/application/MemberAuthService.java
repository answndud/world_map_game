package com.worldmap.auth.application;

import com.worldmap.auth.domain.Member;
import com.worldmap.auth.domain.MemberRepository;
import com.worldmap.auth.domain.MemberRole;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberAuthService {

	private final MemberRepository memberRepository;
	private final MemberPasswordHasher memberPasswordHasher;
	private final MemberCredentialPolicy memberCredentialPolicy;

	public MemberAuthService(
		MemberRepository memberRepository,
		MemberPasswordHasher memberPasswordHasher,
		MemberCredentialPolicy memberCredentialPolicy
	) {
		this.memberRepository = memberRepository;
		this.memberPasswordHasher = memberPasswordHasher;
		this.memberCredentialPolicy = memberCredentialPolicy;
	}

	@Transactional
	public Member signUp(String rawNickname, String rawPassword) {
		String nickname = memberCredentialPolicy.normalizeNickname(rawNickname);
		memberCredentialPolicy.validatePassword(rawPassword);

		if (memberRepository.existsByNicknameIgnoreCase(nickname)) {
			throw new IllegalStateException("이미 사용 중인 닉네임입니다.");
		}

		Member member = Member.create(nickname, memberPasswordHasher.hash(rawPassword), MemberRole.USER);
		member.markLoggedIn(LocalDateTime.now());
		return memberRepository.save(member);
	}

	@Transactional
	public Member login(String rawNickname, String rawPassword) {
		String nickname = memberCredentialPolicy.normalizeNickname(rawNickname);
		memberCredentialPolicy.validatePassword(rawPassword);

		Member member = memberRepository.findByNicknameIgnoreCase(nickname)
			.orElseThrow(() -> new IllegalArgumentException("닉네임 또는 비밀번호가 올바르지 않습니다."));

		if (!memberPasswordHasher.matches(rawPassword, member.getPasswordHash())) {
			throw new IllegalArgumentException("닉네임 또는 비밀번호가 올바르지 않습니다.");
		}

		member.markLoggedIn(LocalDateTime.now());
		return member;
	}
}
