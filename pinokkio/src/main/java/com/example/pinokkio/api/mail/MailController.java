package com.example.pinokkio.api.mail;

import com.example.pinokkio.api.mail.request.MailAuthRequest;
import com.example.pinokkio.api.mail.request.MailRequest;
import com.example.pinokkio.api.mail.response.MailAuthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Mail Controller", description = "메일 인증 관련 API")
@RequestMapping("/api/mail")
public class MailController {

    private final MailService mailService;
    private static final String NOT_IDENTIFIED = "SIGNUP";
    private static final String POS_IDENTIFIED = "POS";
    private static final String TELLER_IDENTIFIED = "TELLER";


    @Operation(summary = "인증 메일 전송", description = "이메일로 인증 번호를 전송.")
    @PostMapping("/send")
    public ResponseEntity<?> sendMail(@RequestBody MailRequest mailRequest) {
        try {
            mailService.sendEmail(mailRequest.getEmail(), NOT_IDENTIFIED);
            return ResponseEntity.ok().build();
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Failed to send email: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send email");
        }
    }


    @Operation(summary = "포스 새로운 비밀번호 전송", description = "포스 이메일로 새로운 비밀번호를 전송.")
    @PostMapping("/send/pos/new-password")
    public ResponseEntity<?> sendPosPasswordUpdateMail(@RequestBody MailRequest mailRequest) {
        try {
            mailService.sendEmail(mailRequest.getEmail(), POS_IDENTIFIED);
            return ResponseEntity.ok().build();
        } catch (MessagingException | UnsupportedEncodingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send email");
        }
    }


    @Operation(summary = "상담원 새로운 비밀번호 전송", description = "상담원 이메일로 새로운 비밀번호를 전송.")
    @PostMapping("/send/teller/new-password")
    public ResponseEntity<?> sendTellerPasswordUpdateMail(@RequestBody MailRequest mailRequest) {
        try {
            mailService.sendEmail(mailRequest.getEmail(), TELLER_IDENTIFIED);
            return ResponseEntity.ok().build();
        } catch (MessagingException | UnsupportedEncodingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send email");
        }
    }


    @Operation(summary = "인증 번호 확인", description = "인증 번호의 유효성을 확인합니다.")
    @PostMapping("/check-auth")
    public ResponseEntity<MailAuthResponse> checkAuth(
            @RequestBody MailAuthRequest mailAuthRequest) {
        boolean isAuthenticated = mailService.isAuthenticated(mailAuthRequest.getAuthNum());
        return ResponseEntity.ok(new MailAuthResponse(isAuthenticated));
    }


}
