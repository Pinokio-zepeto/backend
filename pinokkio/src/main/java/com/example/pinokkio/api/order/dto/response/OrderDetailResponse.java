package com.example.pinokkio.api.order.dto.response;

import com.example.pinokkio.api.order.Order;
import com.example.pinokkio.common.type.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class OrderDetailResponse {
    private final UUID orderId;
    private final UUID customerId;
    private final long totalAmount;
    private final LocalDateTime orderTime;
    private final LocalDateTime cancelTime;
    private final OrderStatus status;
    private List<OrderItemDetail> items;

    public OrderDetailResponse(Order order) {
        this.orderId = order.getId();
        this.customerId = order.getCustomer().getId();
        this.totalAmount = order.getTotalPrice();
        this.orderTime = order.getCreatedDate();
        this.status = order.getStatus();
        this.cancelTime = order.getModifiedDate();
        this.items = new ArrayList<>();
    }

    public void updateItems(List<OrderItemDetail> items) {
        this.items = items;
    }
}
