package com.example.pinokkio.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;

/**
 * 액세스 권한이 없는 리소스에 접근할 경우 발생하는 예외 클래스
 */
@Slf4j
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       org.springframework.security.access.AccessDeniedException accessDeniedException) throws IOException {

        log.info("[handle] 접근이 막혔을 경우 에러 throw");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/text");
        response.setCharacterEncoding("utf-8");
        response.getWriter().print(accessDeniedException.getMessage());
    }
}
