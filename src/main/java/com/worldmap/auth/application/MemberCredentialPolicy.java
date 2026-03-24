package com.worldmap.auth.application;

import org.springframework.stereotype.Component;

@Component
public class MemberCredentialPolicy {

	public String normalizeNickname(String rawNickname) {
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

	public void validatePassword(String rawPassword) {
		if (rawPassword == null || rawPassword.isBlank()) {
			throw new IllegalArgumentException("비밀번호를 입력해주세요.");
		}
		if (rawPassword.length() < 4 || rawPassword.length() > 100) {
			throw new IllegalArgumentException("비밀번호는 4자 이상 100자 이하로 입력해주세요.");
		}
	}
}
