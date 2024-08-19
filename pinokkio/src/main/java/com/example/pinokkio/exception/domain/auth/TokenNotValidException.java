package com.example.pinokkio.exception.domain.auth;

import com.example.pinokkio.exception.base.AuthorizationException;

/**
 * 403 UNAUTHORIZED
 */
public class TokenNotValidException extends AuthorizationException {
    // Access Token 유효성 검사 실패
    public TokenNotValidException() {
        super("UNAUTHORIZED_TOKEN_02", "AccessToken 유효성 검사에 실패하였습니다.");
    }

    // Refresh Token 유효성 검사 실패
    public TokenNotValidException(boolean isRefreshToken) {
        super("UNAUTHORIZED_REFRESH_TOKEN_01", "RefreshToken 유효성 검사에 실패하였습니다.");
    }
}
