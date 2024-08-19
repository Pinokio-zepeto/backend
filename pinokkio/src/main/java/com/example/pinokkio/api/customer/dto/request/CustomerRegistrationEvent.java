package com.example.pinokkio.api.customer.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "고객 등록 요청 이벤트 DTO")
public class CustomerRegistrationEvent {

    private final CustomerRegistrationRequest request;
}
