package com.example.pinokkio.api.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@Schema(description = "로그인 요청 DTO")
public class LoginRequest {
    @Schema(description = "사용자 이메일", example = "user@ssafy.com", required = true)
    private String username;

    @Schema(description = "비밀번호", example = "password123", required = true)
    private String password;
}