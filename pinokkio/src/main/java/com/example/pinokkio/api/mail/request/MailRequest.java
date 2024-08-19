package com.example.pinokkio.api.mail.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Schema(description = "메일 전송 요청 DTO")
public class MailRequest {
    @Schema(description = "이메일 주소", example = "user@ssafy.com", required = true)
    private String email;
}