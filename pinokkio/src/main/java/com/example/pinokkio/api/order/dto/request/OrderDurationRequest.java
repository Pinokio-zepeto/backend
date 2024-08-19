package com.example.pinokkio.api.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "특정 기간 동안의 주문 내역 조회를 위한 요청 DTO")
public class OrderDurationRequest {

    @Schema(description = "조회 시작 기간", example = "2024-07-01")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String startDate;

    @Schema(description = "조회 종료 기간", example = "2024-08-16")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String endDate;
}