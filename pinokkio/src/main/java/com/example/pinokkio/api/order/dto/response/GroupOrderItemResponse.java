package com.example.pinokkio.api.order.dto.response;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Schema(description = "주문리스트 응답 DTO")
public class GroupOrderItemResponse {

    @Schema(description = "고객 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private final UUID customerId;

    @Schema(description = "주문아이템 응답 DTO")
    private final List<OrderItemResponse> orderItems;

    public GroupOrderItemResponse(UUID customerId, List<OrderItemResponse> orderItemResponses) {
        this.customerId = customerId;
        this.orderItems = orderItemResponses;
    }
}
