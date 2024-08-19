package com.example.pinokkio.api.mail.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Schema(description = "메일 인증 요청 DTO")
public class MailAuthRequest {
    @Schema(description = "인증 번호", example = "1B9EbZ", required = true)
    private String authNum;
}