package com.example.pinokkio.exception.domain.kiosk;

import com.example.pinokkio.exception.base.NotFoundException;

import java.util.Map;
import java.util.UUID;

/**
 * 404 NOT FOUND
 */
public class KioskNotFoundException extends NotFoundException {
    public KioskNotFoundException(UUID kioskId) {
        super(
                "NOT_FOUND_KIOSK_01",
                "아이디에 부합한 키오스크를 찾을 수 없습니다.",
                Map.of("kioskId", String.valueOf(kioskId))
        );
    }

    public KioskNotFoundException(String email) {
        super(
                "NOT_FOUND_KIOSK_01",
                "이메일에 부합한 키오스크를 찾을 수 없습니다.",
                Map.of("email", String.valueOf(email))
        );
    }
}