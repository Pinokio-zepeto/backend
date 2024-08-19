package com.example.pinokkio.api.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "주문 정보 요청 DTO")
public class GroupOrderItemRequest {
    //nullable
    private UUID customerId;
    private List<OrderItemRequest> orderItems = new ArrayList<>();
}
