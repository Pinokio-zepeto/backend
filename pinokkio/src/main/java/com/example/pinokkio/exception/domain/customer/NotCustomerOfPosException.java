package com.example.pinokkio.exception.domain.customer;

import com.example.pinokkio.exception.base.BadInputException;

import java.util.Map;
import java.util.UUID;

public class NotCustomerOfPosException extends BadInputException {
    public NotCustomerOfPosException(UUID customerId) {
        super(
                "BAD_INPUT_CUSTOMER_01",
                "현재 로그인 중인 포스의 고객 정보가 아닙니다.",
                Map.of("customerId", String.valueOf(customerId))
        );
    }
}
