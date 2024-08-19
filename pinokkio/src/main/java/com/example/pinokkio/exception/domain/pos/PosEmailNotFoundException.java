package com.example.pinokkio.exception.domain.pos;

import com.example.pinokkio.exception.base.NotFoundException;

import java.util.Map;

/**
 * 404 NOT FOUND
 */
public class PosEmailNotFoundException extends NotFoundException {
    public PosEmailNotFoundException(String email) {
        super(
                "NOT_FOUND_POS_02",
                "이메일에 부합한 포스를 찾을 수 없습니다.",
                Map.of("email", email)
        );
    }
}
