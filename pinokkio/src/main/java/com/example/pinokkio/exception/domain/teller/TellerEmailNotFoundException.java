package com.example.pinokkio.exception.domain.teller;

import com.example.pinokkio.exception.base.NotFoundException;

import java.util.Map;

public class TellerEmailNotFoundException extends NotFoundException {
    public TellerEmailNotFoundException(String email) {
        super(
                "NOT_FOUND_TELLER_02",
                "이메일에 부합한 상담원을 찾을 수 없습니다.",
                Map.of("email", email)
        );
    }
}
