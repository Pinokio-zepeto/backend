package com.example.pinokkio.api.teller;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TellerRepository extends JpaRepository<Teller, UUID> {
    /**
     * 해당 이메일로 가입된 상담원을 반환한다.
     * @param email 이메일 정보
     * @return 가입 여부 정보
     */
    Optional<Teller> findByEmail(String email);

    /**
     * 해당 이메일로 가입된 상담원 존재 여부를 확인한다.
     * @param email 이메일 정보
     * @return 가입 여부 정보
     */
    boolean existsByEmail(String email);
}
