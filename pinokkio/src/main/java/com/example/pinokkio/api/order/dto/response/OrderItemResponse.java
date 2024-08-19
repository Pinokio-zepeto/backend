package com.example.pinokkio.api.order.dto.response;

import com.example.pinokkio.api.order.orderitem.OrderItem;
import lombok.Getter;

import java.util.UUID;

@Getter
public class OrderItemResponse {
    private final UUID itemId; // 아이템 ID
    private final int quantity; // 주문한 수량
    public OrderItemResponse(OrderItem orderItem) {
        this.itemId = orderItem.getItem().getId();
        this.quantity = orderItem.getQuantity();
    }
}
