package com.example.pinokkio.api.customer.dto.response;

import com.example.pinokkio.api.customer.Customer;
import com.example.pinokkio.api.item.Item;
import com.example.pinokkio.common.type.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.UUID;

@Getter
@Schema(description = "고객 응답 데이터")
public class CustomerResponse {

    @Schema(description = "고객 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private final UUID customerId;

    @Schema(description = "성별", example = "FEMALE")
    private final Gender gender;

    @Schema(description = "전화번호 8자리", example = "12345678")
    private final String phoneNumber;

    @Schema(description = "나이", example = "20")
    private final int age;

    public CustomerResponse(Customer customer) {
        this.customerId = customer.getId();
        this.gender = customer.getGender();
        this.phoneNumber = customer.getPhoneNumber();
        this.age = customer.getAge();
    }
}
