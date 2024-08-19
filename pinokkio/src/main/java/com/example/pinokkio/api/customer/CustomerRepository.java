package com.example.pinokkio.api.customer;

import com.example.pinokkio.common.type.Gender;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    /**
     * 특정 POS ID, 성별 및 나이 범위에 해당하는 고객 목록을 조회합니다.
     *
     * @param posId    POS ID
     * @param gender   성별
     * @param minAge   최소 나이
     * @param maxAge   최대 나이
     * @return 조건에 맞는 고객 목록
     */
    List<Customer> findByPosIdAndGenderAndAgeBetween(UUID posId, Gender gender, int minAge, int maxAge);

    /**
     * 특정 POS ID에 해당하는 모든 고객 목록을 조회합니다.
     *
     * @param posId POS ID
     * @return 해당 POS ID의 모든 고객 목록
     */
    List<Customer> findAllByPosId(UUID posId);

    /**
     * 특정 POS ID와 전화번호를 가진 고객을 조화합니다.
     *
     * @param posId POS ID
     * @param phoneNumber 고객의 전화번호(8자리)
     * @return 해당 조건에 부합하는 고객
     */
    Optional<Customer> findByPosIdAndPhoneNumber(UUID posId, String phoneNumber);
}
