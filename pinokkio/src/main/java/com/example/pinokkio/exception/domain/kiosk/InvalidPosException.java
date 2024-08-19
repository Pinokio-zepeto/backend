package com.example.pinokkio.exception.domain.kiosk;

import com.example.pinokkio.exception.base.NotFoundException;

import java.util.Map;
import java.util.UUID;

/**
 * 404 NOT FOUND
 */
public class InvalidPosException extends NotFoundException {
    public InvalidPosException() {
        super(
                "INVALID_KIOSK_01",
                "키오스크의 POS ID가 적절하지 않습니다."
        );
    }
}