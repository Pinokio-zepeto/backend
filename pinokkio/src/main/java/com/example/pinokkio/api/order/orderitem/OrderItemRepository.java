package com.example.pinokkio.api.order.orderitem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    /**
     * 특정 고객이 가장 많이 주문한 아이템 리스트를 순서대로 반환한다.
     * [아이템, 주문량]
     * @param customerId 고객 식별자
     * @return 특정 고객이 가장 많이 주문한 아이템 목록
     */
    @Query("SELECT oi.item, SUM(oi.quantity) AS totalQuantity " +
            "FROM OrderItem oi " +
            "WHERE oi.customerId = :customerId " +
            "GROUP BY oi.item " +
            "ORDER BY totalQuantity DESC")
    List<Object[]> findTopOrderedItemsByCustomerId(@Param("customerId") UUID customerId);

}
