package com.worldmap.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(
	name = "member_account",
	indexes = {
		@Index(name = "idx_member_account_nickname", columnList = "nickname", unique = true)
	}
)
public class Member {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 20)
	private String nickname;

	@Column(name = "password_hash", nullable = false, length = 120)
	private String passwordHash;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private MemberRole role;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "last_login_at")
	private LocalDateTime lastLoginAt;

	protected Member() {
	}

	private Member(
		String nickname,
		String passwordHash,
		MemberRole role,
		LocalDateTime createdAt
	) {
		this.nickname = nickname;
		this.passwordHash = passwordHash;
		this.role = role;
		this.createdAt = createdAt;
	}

	public static Member create(String nickname, String passwordHash, MemberRole role) {
		return new Member(nickname, passwordHash, role, LocalDateTime.now());
	}

	public void markLoggedIn(LocalDateTime loggedInAt) {
		this.lastLoginAt = loggedInAt;
	}

	public void provisionAdmin(String passwordHash) {
		this.passwordHash = passwordHash;
		this.role = MemberRole.ADMIN;
	}

	public Long getId() {
		return id;
	}

	public String getNickname() {
		return nickname;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public MemberRole getRole() {
		return role;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getLastLoginAt() {
		return lastLoginAt;
	}
}
