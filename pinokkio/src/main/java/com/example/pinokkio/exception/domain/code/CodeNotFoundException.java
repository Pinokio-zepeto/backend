package com.example.pinokkio.exception.domain.code;

import com.example.pinokkio.exception.base.NotFoundException;

import java.util.Map;
import java.util.UUID;

/**
 * 404 NOT FOUND
 */
public class CodeNotFoundException extends NotFoundException {
    public CodeNotFoundException(String codeId) {
        super(
                "NOT_FOUND_CODE_01",
                "아이디에 부합한 코드를 찾을 수 없습니다.",
                Map.of("codeId", String.valueOf(codeId))
        );
    }
}
