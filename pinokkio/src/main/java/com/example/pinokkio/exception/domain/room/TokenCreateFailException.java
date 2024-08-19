package com.example.pinokkio.exception.domain.room;

import com.example.pinokkio.exception.base.NotFoundException;

import java.util.Map;

public class TokenCreateFailException extends NotFoundException {
    public TokenCreateFailException() {
        super("TOKEN_CREATE_FAIL_01", "OpenVidu 내 오류로 토큰 발급이 불가합니다.");
    }
}
