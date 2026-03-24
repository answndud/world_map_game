package com.worldmap.auth.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

	Optional<Member> findByNicknameIgnoreCase(String nickname);

	boolean existsByNicknameIgnoreCase(String nickname);
}
