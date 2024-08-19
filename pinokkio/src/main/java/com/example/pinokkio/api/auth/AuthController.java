package com.example.pinokkio.api.auth;

import com.example.pinokkio.api.auth.dto.request.LoginRequest;
import com.example.pinokkio.api.auth.dto.request.SignUpPosRequest;
import com.example.pinokkio.api.auth.dto.request.SignUpTellerRequest;
import com.example.pinokkio.api.auth.dto.response.KioskLoginResponse;
import com.example.pinokkio.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth Controller", description = "로그인 및 회원가입 API")
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "POS 회원가입", description = "POS 사용자를 위한 회원가입을 처리")
    @PostMapping("/register/pos")
    public ResponseEntity<?> registerPos(@Validated @RequestBody SignUpPosRequest signUpPosRequest) {
        authService.registerPos(signUpPosRequest);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Operation(summary = "상담원 회원가입", description = "상담원을 위한 회원가입을 처리")
    @PostMapping("/register/teller")
    public ResponseEntity<?> registerTeller(@Validated @RequestBody SignUpTellerRequest signUpTellerRequest) {
        authService.registerTeller(signUpTellerRequest);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Operation(summary = "POS 로그인", description = "POS 사용자 로그인을 처리")
    @PostMapping("/login/pos")
    public ResponseEntity<?> loginPos(@Validated @RequestBody LoginRequest loginRequest) {
        AuthToken authToken = authService.loginPos(loginRequest);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer " + authToken.getAccessToken());
        return new ResponseEntity<>(authToken, httpHeaders, HttpStatus.OK);
    }

    @Operation(summary = "상담원 로그인", description = "상담원 로그인을 처리")
    @PostMapping("/login/teller")
    public ResponseEntity<?> loginTeller(@Validated @RequestBody LoginRequest loginRequest) {
        AuthToken authToken = authService.loginTeller(loginRequest);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer " + authToken.getAccessToken());
        return new ResponseEntity<>(authToken, httpHeaders, HttpStatus.OK);
    }

    @Operation(summary = "키오스크 로그인", description = "키오스크 로그인을 처리")
    @PostMapping("/login/kiosk")
    public ResponseEntity<?> loginKiosk(@Validated @RequestBody LoginRequest loginRequest) {
        KioskLoginResponse loginResponse = authService.loginKiosk(loginRequest);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer " + loginResponse.getAuthToken().getAccessToken());
        return new ResponseEntity<>(loginResponse, httpHeaders, HttpStatus.OK);
    }

    @Operation(summary = "토큰 재발급", description = "refresh 토큰을 이용한 토큰 재발급")
    @GetMapping("/refresh")
    public ResponseEntity<?> tokenReissue(HttpServletRequest request) {
        String refresh = request.getHeader("refresh");
        AuthToken authToken = authService.reissue(refresh);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer " + authToken.getAccessToken());
        return new ResponseEntity<>(authToken, httpHeaders, HttpStatus.OK);
    }
}