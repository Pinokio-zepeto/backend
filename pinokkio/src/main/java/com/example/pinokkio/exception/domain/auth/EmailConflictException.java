package com.example.pinokkio.exception.domain.auth;

import com.example.pinokkio.exception.base.ConflictException;

import java.util.Map;

/**
 * 409 CONFLICT
 */
public class EmailConflictException extends ConflictException {
    public EmailConflictException(String email) {
        super("CONFLICT_EMAIL_01", "중복된 이메일입니다.", Map.of("email", email));
    }
}
