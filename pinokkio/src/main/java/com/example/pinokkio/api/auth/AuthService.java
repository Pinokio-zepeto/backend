package com.example.pinokkio.api.auth;

import com.example.pinokkio.api.auth.dto.request.LoginRequest;
import com.example.pinokkio.api.auth.dto.request.SignUpPosRequest;
import com.example.pinokkio.api.auth.dto.request.SignUpTellerRequest;
import com.example.pinokkio.api.auth.dto.response.KioskLoginResponse;
import com.example.pinokkio.api.customer.Customer;
import com.example.pinokkio.api.customer.CustomerRepository;
import com.example.pinokkio.api.kiosk.Kiosk;
import com.example.pinokkio.api.kiosk.KioskRepository;
import com.example.pinokkio.api.pos.Pos;
import com.example.pinokkio.api.pos.PosRepository;
import com.example.pinokkio.api.pos.code.Code;
import com.example.pinokkio.api.pos.code.CodeRepository;
import com.example.pinokkio.api.teller.Teller;
import com.example.pinokkio.api.teller.TellerRepository;
import com.example.pinokkio.common.type.Gender;
import com.example.pinokkio.config.RedisUtil;
import com.example.pinokkio.config.jwt.JwtProvider;
import com.example.pinokkio.config.jwt.Role;
import com.example.pinokkio.exception.base.AuthenticationException;
import com.example.pinokkio.exception.domain.auth.EmailConflictException;
import com.example.pinokkio.exception.domain.auth.PasswordBadInputException;
import com.example.pinokkio.exception.domain.code.CodeNotFoundException;
import com.example.pinokkio.exception.domain.kiosk.KioskNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final long accessValidTime = 1000L * 60 * 60;    // 액세스 토큰 유효 시간 60분
    private final long refreshValidTime = 1000L * 60 * 60 * 24 * 14;    // 리프레쉬 토큰 유효 시간 2주

    private final PasswordEncoder passwordEncoder;

    private final RedisUtil redisUtil;
    private final JwtProvider jwtProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    private final CodeRepository codeRepository;
    private final PosRepository posRepository;
    private final KioskRepository kioskRepository;
    private final TellerRepository tellerRepository;


    private final CustomerRepository customerRepository;

    /**
     * 가맹 코드, 이메일, 비밀번호, 비밀번호 확인 정보를 바탕으로 회원가입을 진행한다.
     *
     * @param signUpPosRequest 포스 회원가입을 위한 Dto
     */
    @Transactional
    public void registerPos(SignUpPosRequest signUpPosRequest) {
        Code requestCode = checkValidateCode(signUpPosRequest.getCode());
        checkDuplicateEmail(signUpPosRequest.getUsername(), "ROLE_POS");
        checkConfirmPassword(signUpPosRequest.getPassword(), signUpPosRequest.getConfirmPassword());
        Pos pos = Pos.builder()
                .email(signUpPosRequest.getUsername())
                .password(passwordEncode(signUpPosRequest.getPassword()))
                .code(requestCode)
                .build();
        Pos savedPos = posRepository.save(pos);

        Customer customer = Customer.builder()
                .phoneNumber("00000000")
                .pos(pos)
                .age(99)
                .gender(Gender.MALE)
                .faceEmbedding(null)
                .build();
        Customer savedCustomer = customerRepository.save(customer);
        savedPos.updateDummyCustomerUUID(savedCustomer.getId());
    }

    /**
     * 가맹 코드, 이메일, 비밀번호, 비밀번호 확인 정보를 바탕으로 회원가입을 진행한다.
     *
     * @param signUpTellerRequest 상담원 회원가입을 위한 Dto
     */
    @Transactional
    public void registerTeller(SignUpTellerRequest signUpTellerRequest) {
        Code requestCode = checkValidateCode(signUpTellerRequest.getCode());
        checkDuplicateEmail(signUpTellerRequest.getUsername(), "ROLE_TELLER");
        checkConfirmPassword(signUpTellerRequest.getPassword(), signUpTellerRequest.getConfirmPassword());
        Teller teller = Teller.builder()
                .email(signUpTellerRequest.getUsername())
                .password(passwordEncode(signUpTellerRequest.getPassword()))
                .code(requestCode)
                .build();
        tellerRepository.save(teller);
        log.info("SAVE TELLER: {}", teller);
    }

    @Transactional
    public AuthToken loginPos(LoginRequest loginRequest) {
        try {
            Role role = Role.P;
            String posEmail = role + loginRequest.getUsername();

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(
                            posEmail,
                            loginRequest.getPassword()
                    );
            log.info("new email: {}", posEmail);
            log.info("authToken: {}", authenticationToken);

            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
            log.info("authenticate 완료: {}", authentication);

            SecurityContextHolder.getContext().setAuthentication(authentication);

            AuthToken authToken = createTokens(authentication.getName(), role.getValue());
            // 리프레시 토큰을 Redis에 저장
            saveRefreshTokenToRedis(authentication.getName(), role.getValue(), authToken.getAccessToken(), authToken.getRefreshToken());

            return authToken;
        } catch (AuthenticationException e) {
            log.error("POS 인증 실패", e);
            throw new AuthenticationException("POS 인증 실패", e.getMessage());
        } catch (Exception e) {
            log.error("인증 과정에서 예외 발생", e);
            throw new RuntimeException("인증 과정에서 예외 발생", e);
        }
    }


    @Transactional
    public KioskLoginResponse loginKiosk(LoginRequest loginRequest) {
        try {
            Role role = Role.K;
            String kioskEmail = role + loginRequest.getUsername();

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(
                            kioskEmail,
                            loginRequest.getPassword()
                    );
            log.info("new email: {}", kioskEmail);
            log.info("authToken: {}", authenticationToken);

            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
            log.info("authenticate 완료: {}", authentication);

            SecurityContextHolder.getContext().setAuthentication(authentication);

            AuthToken authToken = createTokens(authentication.getName(), role.getValue());
            // 리프레시 토큰을 Redis에 저장
            saveRefreshTokenToRedis(authentication.getName(), role.getValue(), authToken.getAccessToken(), authToken.getRefreshToken());

            // 키오스크 ID
            String userEmail = authentication.getName();
            UUID kioskId = kioskRepository.findByEmail(userEmail)
                    .map(Kiosk::getId)
                    .orElseThrow(() -> new KioskNotFoundException(userEmail));

            return new KioskLoginResponse(authToken, kioskId);
        } catch (AuthenticationException e) {
            throw new AuthenticationException("KIOSK 인증 실패", e.getMessage());
        }
    }

    @Transactional
    public AuthToken loginTeller(LoginRequest loginRequest) {
        try {
            Role role = Role.T;
            String tellerEmail = role + loginRequest.getUsername();

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(
                            tellerEmail,
                            loginRequest.getPassword()
                    );
            log.info("new email: {}", tellerEmail);
            log.info("authToken: {}", authenticationToken);

            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
            log.info("authenticate 완료: {}", authentication);

            SecurityContextHolder.getContext().setAuthentication(authentication);

            AuthToken authToken = createTokens(authentication.getName(), role.getValue());
            // 리프레시 토큰을 Redis에 저장
            saveRefreshTokenToRedis(authentication.getName(), role.getValue(), authToken.getAccessToken(), authToken.getRefreshToken());
            return authToken;
        } catch (AuthenticationException e) {
            throw new AuthenticationException("TELLER 인증 실패", e.getMessage());
        }
    }

    /**
     * 토큰 정보를 기반으로 리프레시 토큰을 Redis 에 저장한다.
     *
     * @param username     유저 아이디(email)
     * @param role         유저 타입
     * @param accessToken  엑세스 토큰 정보
     * @param refreshToken 리프레시 토큰 정보
     */
    private void saveRefreshTokenToRedis(String username, String role, String accessToken, String refreshToken) {
        String key = "refreshToken:" + username + ":" + role + ":" + DigestUtils.sha256Hex(accessToken);
        redisUtil.setDataExpire(key, refreshToken, 1000L * 60 * 60 * 24 * 14);
        log.info("[AuthService] 리프레시 토큰을 Redis에 저장: {}", key);
    }

    /**
     * 토큰 정보를 기반으로 리프레시 토큰을 Redis 에 갱신한다.
     *
     * @param username     유저 아이디(email)
     * @param role         유저 타입
     * @param accessToken  엑세스 토큰 정보
     * @param refreshToken 리프레시 토큰 정보
     */
    private void renewalTokenToRedis(String username, String role, String accessToken, String refreshToken) {
        // 이전에 저장된 리프레시 토큰을 삭제
        String existingTokenKeyPattern = "refreshToken:" + username + ":" + role + ":*";
        redisUtil.deleteDataByPattern(existingTokenKeyPattern);
        log.info("[AuthService] 기존 리프레시 토큰을 삭제: 패턴={}", existingTokenKeyPattern);

        // 새로운 리프레시 토큰을 저장
        saveRefreshTokenToRedis(username, role, accessToken, refreshToken);
    }

    /**
     * 아이디 중복인 경우 EmailConflictException 을 발생시킨다.
     *
     * @param email 이메일
     * @param role  유저 타입
     */
    public void checkDuplicateEmail(String email, String role) {
        Map<String, Predicate<String>> roleCheckers = Map.of(
                "ROLE_POS", posRepository::existsByEmail,
                "ROLE_TELLER", tellerRepository::existsByEmail,
                "ROLE_KIOSK", kioskRepository::existsByEmail
        );

        Predicate<String> emailChecker = roleCheckers.get(role);
        if (emailChecker != null && emailChecker.test(email)) {
            throw new EmailConflictException(email);
        }
    }

    /**
     * 코드가 유효하지 않은 경우 CodeNotFoundException 을 발생시킨다.
     *
     * @param code 코드
     */
    public Code checkValidateCode(UUID code) {
        return codeRepository
                .findById(code)
                .orElseThrow(() -> new CodeNotFoundException(code.toString()));
    }

    /**
     * 입력받은 두 비밀번호가 같지 않으면 PasswordBadInputException 을 발생시킨다.
     *
     * @param password        비밀번호
     * @param confirmPassword 비밀번호 확인
     */
    public void checkConfirmPassword(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) throw new PasswordBadInputException(password);
    }

    /**
     * BCryptPasswordEncoder 로 비밀번호를 암호화한다.
     *
     * @param password 비밀번호
     * @return 암호화된 비밀번호
     */
    public String passwordEncode(String password) {
        return passwordEncoder.encode(password);
    }

    /**
     * refresh토큰으로 access, refresh 토큰 재발급
     *
     * @param refreshToken refresh 토큰
     */
    public AuthToken reissue(String refreshToken) {
        jwtProvider.validateToken(refreshToken, "refresh");

        String email = jwtProvider.getEmailFromToken(refreshToken);
        String role = jwtProvider.getRoleFromToken(refreshToken);

        AuthToken newTokens = createTokens(email, role);
        renewalTokenToRedis(email, role, newTokens.getAccessToken(), newTokens.getRefreshToken());

        return newTokens;
    }

    private AuthToken createTokens(String email, String role) {
        String newAccessToken = jwtProvider.createJwt("access", email, role, accessValidTime);
        String newRefreshToken = jwtProvider.createJwt("refresh", email, role, refreshValidTime);
        return new AuthToken(newAccessToken, newRefreshToken);
    }
}