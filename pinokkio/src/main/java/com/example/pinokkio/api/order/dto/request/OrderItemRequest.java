package com.example.pinokkio.api.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "주문 정보 요청 DTO")
public class OrderItemRequest {

    @Schema(description = "아이템 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID itemId; // 아이템 ID

    @Schema(description = "주문 수량", example = "10")
    private int quantity; // 주문한 수량
}
