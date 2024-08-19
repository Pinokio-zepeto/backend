package com.example.pinokkio.api.pos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PosRepository extends JpaRepository<Pos, UUID> {

    /**
     * 해당 이메일로 가입된 포스를 반환한다.
     * @param email 이메일 정보
     * @return 가입된 포스 정보
     */
    Optional<Pos> findByEmail(String email);

    /**
     * 해당 이메일로 가입된 포스 존재 여부를 확인한다.
     * @param email 이메일 정보
     * @return 가입 여부 정보
     */
    boolean existsByEmail(String email);

    /**
     * 특정 codeId 를 가지는 Pos 리스트를 반환한다.
     * @param codeId 코드 식별자
     * @return 특정 codeId 를 가지는 Pos 리스트
     */
    List<Pos> findByCodeId(UUID codeId); // 특정 code_id를 가진 모든 Pos 를 찾는 메소드

    @Query("SELECT p.id " +
            "FROM Pos p ")
    List<UUID> findAllPosId();
}
