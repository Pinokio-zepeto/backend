package com.example.pinokkio.config.jwt;

import com.example.pinokkio.config.RedisUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogoutService implements LogoutHandler {

    private final JwtProvider jwtProvider;
    private final RedisUtil redisUtil;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response,
                       Authentication authentication) {

        String accessToken = jwtProvider.resolveAccessToken(request);
        CustomUserDetail customUserDetail = (CustomUserDetail) jwtProvider.getUserFromAccessToken(accessToken);

        // AccessToken 의 해시값을 생성 -> Role 은 다른데 username 이 같은 경우를 방지
        String tokenHash = DigestUtils.sha256Hex(accessToken);

        // 고유한 refreshTokenKey 생성
        String refreshTokenKey = "refreshToken:" + customUserDetail.getUsername() + ":" + customUserDetail.getRole() + ":" + tokenHash;

        // Redis 에서 RefreshToken 조회
        if (redisUtil.existData(refreshTokenKey)) {
            String storedToken = redisUtil.getData(refreshTokenKey);
            log.info("[logoutService] RefreshToken 조회: {}", storedToken);

            // RefreshToken 삭제
            log.info("[logoutService] RefreshToken 삭제");
            redisUtil.deleteData(refreshTokenKey);
            log.info("[logoutService] 로그아웃 완료");
        } else {
            log.warn("[logoutService] 유효하지 않은 RefreshToken 혹은 이미 삭제된 상태입니다.");
        }
    }
}
