package com.example.pinokkio.api.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    /**
     * 특정 고객이 최근에 주문한 순서로 Order 리스트를 반환합니다.
     *
     * @param customerId 고객 식별자
     * @return 최근 주문한 Order 리스트
     */
    List<Order> findByCustomerIdOrderByCreatedDateDesc(UUID customerId);

    /**
     * 해당 포스의 주어진 날짜 범위 내의 모든 Order 리스트를 반환합니다.
     *
     * @param posId POS ID
     * @param startDate 시작 날짜
     * @param endDate   종료 날짜
     * @return 지정된 날짜 범위의 Order 리스트
     */
    List<Order> findAllByPosIdAndCreatedDateBetweenOrderByCreatedDate(UUID posId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 해당 포스의 가장 오래된 주문일자를 반환합니다.
     *
     * @param posId POS ID
     * @return
     */
    @Query("SELECT MIN(o.createdDate) " +
            "FROM Order o " +
            "WHERE o.pos.id = :posId ")
    Optional<LocalDateTime> findOldestOrderDateByPosId(@Param("posId") UUID posId);

    Optional<Order> findByPosIdAndId(UUID posId, UUID orderId);
}
