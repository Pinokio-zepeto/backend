package com.example.pinokkio.exception.domain.teller;

import com.example.pinokkio.exception.base.NotFoundException;

import java.util.Map;
import java.util.UUID;

/**
 * 404 NOT FOUND
 */
public class TellerNotFoundException extends NotFoundException {
    public TellerNotFoundException(UUID tellerId) {
        super(
                "NOT_FOUND_TELLER_01",
                "아이디에 부합한 상담원을 찾을 수 없습니다.",
                Map.of("tellerId", String.valueOf(tellerId))
        );
    }
    
    public TellerNotFoundException(String tellerEmail) {
        super(
                "NOT_FOUND_TELLER_01",
                "이메일에 부합한 상담원을 찾을 수 없습니다.",
                Map.of("tellerEmail", String.valueOf(tellerEmail))
        );
    }
}