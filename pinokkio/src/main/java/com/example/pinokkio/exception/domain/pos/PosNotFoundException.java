package com.example.pinokkio.exception.domain.pos;

import com.example.pinokkio.exception.base.NotFoundException;
import java.util.Map;
import java.util.UUID;

/**
 * 404 NOT FOUND
 */
public class PosNotFoundException extends NotFoundException {
    public PosNotFoundException(String posId) {
        super(
                "NOT_FOUND_POS_01",
                "아이디에 부합한 포스를 찾을 수 없습니다.",
                Map.of("posId", posId)
        );
    }

    public PosNotFoundException(UUID posId) {
        super(
                "NOT_FOUND_POS_01",
                "아이디에 부합한 포스를 찾을 수 없습니다.",
                Map.of("posId", String.valueOf(posId))
        );
    }

}
