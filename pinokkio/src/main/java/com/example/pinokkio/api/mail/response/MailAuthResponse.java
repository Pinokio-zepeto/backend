package com.example.pinokkio.api.mail.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Schema(description = "메일 인증 응답 DTO")
public class MailAuthResponse {
    @Schema(description = "인증 성공 여부", example = "true")
    private boolean isAuthenticated;
}