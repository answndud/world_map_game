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

	public MemberAuthService(MemberRepository memberRepository, MemberPasswordHasher memberPasswordHasher) {
		this.memberRepository = memberRepository;
		this.memberPasswordHasher = memberPasswordHasher;
	}

	@Transactional
	public Member signUp(String rawNickname, String rawPassword) {
		String nickname = normalizeNickname(rawNickname);
		validatePassword(rawPassword);

		if (memberRepository.existsByNicknameIgnoreCase(nickname)) {
			throw new IllegalStateException("이미 사용 중인 닉네임입니다.");
		}

		Member member = Member.create(nickname, memberPasswordHasher.hash(rawPassword), MemberRole.USER);
		member.markLoggedIn(LocalDateTime.now());
		return memberRepository.save(member);
	}

	@Transactional
	public Member login(String rawNickname, String rawPassword) {
		String nickname = normalizeNickname(rawNickname);
		validatePassword(rawPassword);

		Member member = memberRepository.findByNicknameIgnoreCase(nickname)
			.orElseThrow(() -> new IllegalArgumentException("닉네임 또는 비밀번호가 올바르지 않습니다."));

		if (!memberPasswordHasher.matches(rawPassword, member.getPasswordHash())) {
			throw new IllegalArgumentException("닉네임 또는 비밀번호가 올바르지 않습니다.");
		}

		member.markLoggedIn(LocalDateTime.now());
		return member;
	}

	private String normalizeNickname(String rawNickname) {
		if (rawNickname == null) {
			throw new IllegalArgumentException("닉네임을 입력해주세요.");
		}

		String normalized = rawNickname.trim();
		if (normalized.length() < 2 || normalized.length() > 20) {
			throw new IllegalArgumentException("닉네임은 2자 이상 20자 이하로 입력해주세요.");
		}
		if (normalized.chars().anyMatch(Character::isWhitespace)) {
			throw new IllegalArgumentException("닉네임에는 공백을 넣을 수 없습니다.");
		}
		return normalized;
	}

	private void validatePassword(String rawPassword) {
		if (rawPassword == null || rawPassword.isBlank()) {
			throw new IllegalArgumentException("비밀번호를 입력해주세요.");
		}
		if (rawPassword.length() < 4 || rawPassword.length() > 100) {
			throw new IllegalArgumentException("비밀번호는 4자 이상 100자 이하로 입력해주세요.");
		}
	}
}
