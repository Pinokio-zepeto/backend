package com.example.pinokkio.config.jwt;

import com.example.pinokkio.exception.domain.auth.ExpiredTokenException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final HandlerExceptionResolver resolver;

    public JwtAuthenticationEntryPoint(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, IOException {
        if (request.getAttribute("exception") == null) {
            log.info("[commence] 인증 실패로 response.sendError 발생");
            log.info("[Error] {}", authException.getMessage());
            log.info("[request URI] {}", request.getRequestURI());
            log.info("[authException] {}", authException.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/text");
            response.setCharacterEncoding("utf-8");
            response.getWriter().print(new ExpiredTokenException().getMessage());
        } else {
            resolver.resolveException(request, response, null, (Exception) request.getAttribute("exception"));
        }
    }
}
