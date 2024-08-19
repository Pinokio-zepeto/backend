package com.example.pinokkio.api.pos.code;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CodeRepository extends JpaRepository<Code, UUID> {

    /**
     * 입력받은 식별자에 부합하는 코드 존재 여부를 반환한다.
     *
     * @param id 식별자 정보
     * @return 입력받은 식별자에 부합하는 코드 존재 여부
     */
    boolean existsById(@NotNull UUID id);
}
