package com.example.pinokkio.config.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String accessToken = jwtProvider.resolveAccessToken(request);
        log.info("[JwtAuthenticationFilter] AccessToken 값 추출 완료: {}", accessToken);

        try {
            log.info("try 문 진입");
            if (accessToken != null && jwtProvider.validateToken(accessToken, "access")) {
                log.info("if 문 진입");
                Authentication authentication = jwtProvider.getAuthentication(accessToken);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("if 문 종료");
                log.info("authentication={}", authentication);
            }
        } catch (Exception e) {
            log.info("예외발생: {}", e.getMessage());
            request.setAttribute("exception", e);
        }

        log.info("doFilter");
        filterChain.doFilter(request, response);
    }
}
