package com.worldmap.auth.application;

import com.worldmap.auth.domain.MemberRole;

public record AuthenticatedMemberSession(
	Long memberId,
	String nickname,
	MemberRole role
) {
}
