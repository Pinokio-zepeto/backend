package com.example.pinokkio.exception.domain.auth;

import com.example.pinokkio.exception.base.BadInputException;

import java.util.Map;


/**
 * 400 BAD INPUT
 */
public class PasswordBadInputException extends BadInputException {
    public PasswordBadInputException(String password) {
        super("BAD_INPUT_PASSWORD_01", "두 비밀번호가 같지 않습니다.", Map.of("password", password));
    }
}
