package com.example.pinokkio.exception.domain.customer;

import com.example.pinokkio.exception.base.NotFoundException;

import java.util.Map;
import java.util.UUID;

public class CustomerNotFoundException extends NotFoundException {
    public CustomerNotFoundException(UUID customerId) {
        super(
                "NOT_FOUND_CUSTOMER_01",
                "아이디에 부합한 고객을 찾을 수 없습니다.",
                Map.of("customerId", String.valueOf(customerId))
        );
    }

    public CustomerNotFoundException(String phoneNumber) {
        super(
                "NOT_FOUND_CUSTOMER_01",
                "전화번호에 부합한 고객을 찾을 수 없습니다.",
                Map.of("phoneNumber", String.valueOf(phoneNumber))
        );
    }
}
