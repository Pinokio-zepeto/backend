package com.example.pinokkio.exception.domain.order;

import com.example.pinokkio.exception.base.NotFoundException;

import java.util.Map;
import java.util.UUID;

public class OrderNotFoundException extends NotFoundException {
    public OrderNotFoundException(UUID orderId) {
        super(
                "NOT_FOUND_ORDER_01",
                "아이디에 부합한 주문 정보를 찾을 수 없습니다.",
                Map.of("orderId", String.valueOf(orderId))
        );
    }
}
