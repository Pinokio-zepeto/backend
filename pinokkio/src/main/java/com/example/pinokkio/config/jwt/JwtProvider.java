package com.example.pinokkio.config.jwt;

import com.example.pinokkio.api.kiosk.KioskRepository;
import com.example.pinokkio.api.pos.PosRepository;
import com.example.pinokkio.api.teller.TellerRepository;
import com.example.pinokkio.exception.base.AuthenticationException;
import com.example.pinokkio.exception.base.AuthorizationException;
import com.example.pinokkio.exception.domain.auth.ExpiredTokenException;
import com.example.pinokkio.exception.domain.auth.TokenNotValidException;
import com.example.pinokkio.exception.domain.kiosk.KioskNotFoundException;
import com.example.pinokkio.exception.domain.pos.PosNotFoundException;
import com.example.pinokkio.exception.domain.teller.TellerNotFoundException;
import io.jsonwebtoken.*;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtProvider {


    private final KioskRepository kioskRepository;
    private final TellerRepository tellerRepository;
    private final PosRepository posRepository;

    private final CustomUserDetailService customUserDetailService;

    @Value("${jwt.secret}")
    private String secretKey;

    /**
     * secretKey를 Base64로 인코딩
     */
    @PostConstruct
    protected void init() {
        log.info("[init] JwtTokenProvider 내 secretKey 초기화 시작");
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes(StandardCharsets.UTF_8));
        log.info("[init] JwtTokenProvider 내 secretKey 초기화 완료");
    }

    /**
     * 토큰 생성 메서드
     *
     * @param category  access/refresh 토큰 구분
     * @param email     사용자 이메일
     * @param role      역할
     * @param expiredMs 토큰 유효시간
     * @return access/refresh 토큰
     */
    public String createJwt(String category, String email, String role, Long expiredMs) {
        log.info("[createToken] 토큰 생성 시작... 카테고리: " + category);

        Claims claims = Jwts.claims().setSubject(email);
        claims.put("role", role);
        claims.put("category", category);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuer("pinokkio")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public String getCategory(String token) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().get("category", String.class);
    }

    /**
     * AccessToken 추출
     */
    public String resolveAccessToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");

        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.replace("Bearer ", "");
            log.info("[resolveToken] HTTP 헤더에서 Token 값 추출 완료: {}", token);
            return token;
        }
        return null;
    }

    /**
     * AccessToken 로부터 Authentication 객체를 얻어온다.
     */
    public Authentication getAuthentication(String token) {
        log.info("[getAuthentication] 토큰 인증 정보 조회 시작");
        String newInput = parseAndFormatRoleEmail(token);
        UserDetails customUserDetail = customUserDetailService.loadUserByUsername(newInput);

        log.info("[getAuthentication] 토큰 인증 정보 조회 완료, UserDetails User Email : {}", customUserDetail.getUsername());
        return new UsernamePasswordAuthenticationToken(customUserDetail, token, customUserDetail.getAuthorities());
    }

    /**
     * AccessToken 에서 유저정보 추출
     */
    public UserDetails getUserFromAccessToken(String accessToken) {
        try {
            String newInput = parseAndFormatRoleEmail(accessToken);
            return customUserDetailService.loadUserByUsername(newInput);
        } catch (ExpiredJwtException e) {
            String email = e.getClaims().getSubject();
            String role = (String) e.getClaims().get("role");
            Role roleEnum = Role.valueOf(role);
            String newInput = roleEnum + email;
            return customUserDetailService.loadUserByUsername(newInput);
        }
    }

    /**
     * 액세스 토큰으로부터 객체의 ID 반환
     */
    public UUID getUserIDFromToken(String token) {
        log.info("[getUserFromToken] 토큰으로부터 사용자 정보 조회 시작");
        try {
            // 토큰 유효성 검사
            validateToken(token, "access");

            // 토큰에서 이메일과 역할 추출
            String email = getEmailFromToken(token);
            String role = getRoleFromToken(token);

            // UserDetails에서 사용자 정보 추출
            if (role.equals("ROLE_TELLER")) {
                return tellerRepository.findByEmail(email)
                        .orElseThrow(() -> new TellerNotFoundException(email)).getId();
            } else if (role.equals("ROLE_KIOSK")) {
                return kioskRepository.findByEmail(email)
                        .orElseThrow(() -> new KioskNotFoundException(email)).getId();
            } else if (role.equals("ROLE_POS")) {
                return posRepository.findByEmail(email)
                        .orElseThrow(() -> new PosNotFoundException(email)).getId();
            } else {
                throw new IllegalArgumentException("Unknown user role: " + role);
            }

        } catch (ExpiredJwtException e) {
            log.error("[getUserIdFromToken] 토큰이 만료되었습니다.", e);
            throw new ExpiredTokenException();
        } catch (Exception e) {
            log.error("[getUserIdFromToken] 토큰으로부터 사용자 정보 조회 중 오류 발생", e);
            throw new AuthenticationException("AUTH_003", "Failed to get user from token");
        }
    }

    /**
     * AccessToken의 유효성 + 만료여부 체크
     */
    public boolean validateToken(String token, String tokenType) {

        log.info("[validateToken] {} 토큰 유효 체크 시작", tokenType);
        boolean isRefreshToken = tokenType.equals("refresh");

        if (token == null) {
            log.info("[validateToken] {} 토큰 없음", tokenType);
            if (isRefreshToken) {
                throw new TokenNotValidException(true);
            }
            throw new TokenNotValidException();
        }

        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);

            // 토큰 종류 검증
            String category = getCategory(token);
            if (isRefreshToken && !category.equals("refresh")) {
                log.info("[validateToken] {} 토큰 타입 불일치 ", tokenType);
                throw new TokenNotValidException(true);
            } else if (!isRefreshToken && !category.equals("access")) {
                log.info("[validateToken] {} 토큰 타입 불일치 ", tokenType);
                throw new TokenNotValidException();
            }
        } catch (ExpiredJwtException e) {
            log.info("[validateToken] {} 토큰 만료 ", tokenType);
            if (isRefreshToken) {
                throw new ExpiredTokenException(true);
            } else {
                throw new ExpiredTokenException();
            }
        } catch (MalformedJwtException | SignatureException e) {
            log.info("[validateToken] {} 토큰 유효하지 않음 ", tokenType);
            if (isRefreshToken) {
                throw new TokenNotValidException(true);
            } else {
                throw new TokenNotValidException();
            }
        }
        return true;
    }

    /**
     * AccessToken 에서 Email 추출
     */
    public String getEmailFromToken(String token) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getSubject();
    }

    /**
     * AccessToken 에서 Role 추출
     */
    public String getRoleFromToken(String token) {
        return (String) Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().get("role");
    }

    /**
     * 토큰으로부터 이메일(아이디)값과 role 값을 파싱해 "R + 이메일" 형태로 제공
     * R
     * - "P" : ROLE_POS
     * - "K" : ROLE_KIOSK
     * - "T" : ROLE_TELLER
     */
    private String parseAndFormatRoleEmail(String token) {
        String email = getEmailFromToken(token);
        String role = getRoleFromToken(token);
        log.info("[parseToken] email: {}, role: {}", email, role);
        Role parseRole = Role.fromValue(role);

        String parseToken = parseRole + email;
        log.info("[reformat parseToken] parseToken: {}", parseToken);
        return parseToken;
    }

    /**
     * 현재 인증된 사용자로부터 이메일을 가져오는 메서드
     */
    public String getCurrentUserEmail() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getPrincipal)
                .filter(principal -> principal instanceof UserDetails)
                .map(principal -> ((UserDetails) principal).getUsername())
                .orElseThrow(() -> new AuthenticationException("AUTH_001", "User is not authenticated"));
    }

    /**
     * 현재 인증된 사용자로부터 역할을 가져오는 메서드
     */
    public String getCurrentUserRole() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getCredentials)
                .map(Object::toString)
                .map(this::getRoleFromToken)
                .orElseThrow(() -> new AuthorizationException("AUTH_002", "User role not found"));
    }
}
