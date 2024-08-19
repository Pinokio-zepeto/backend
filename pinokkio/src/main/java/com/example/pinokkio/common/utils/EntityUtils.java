package com.example.pinokkio.common.utils;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.function.Function;

public class EntityUtils {

    /**
     * 제네릭 메서드를 사용하여 다양한 엔티티를 조회하는 로직
     *
     * @param repository       JPA 리포지토리
     * @param id               엔티티 ID (문자열)
     * @param exceptionSupplier 예외를 생성하는 함수
     * @param <T>             엔티티 타입
     * @param <ID>            ID 타입
     * @return 조회된 엔티티
     * @throws RuntimeException 사용자 정의 예외
     */
    public static <T, ID> T getEntityById(JpaRepository<T, UUID> repository, UUID id, Function<UUID, RuntimeException> exceptionSupplier) {
        return repository.findById(id)
                .orElseThrow(() -> exceptionSupplier.apply(id));
    }
}
