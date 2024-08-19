package com.example.pinokkio.api.auth.dto.response;

import com.example.pinokkio.api.auth.AuthToken;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@AllArgsConstructor
@Schema(description = "키오스크 로그인 응답 DTO")
public class KioskLoginResponse {
    @Schema(description = "키오스크 로그인 토큰", example = "{\n" +
            "  \"accessToken\": \"eyJhbG..\",\n" +
            "  \"refreshToken\": \"edfFb...\"\n" +
            "}\n", required = true)
    private AuthToken authToken;

    @Schema(description = "키오스크 UUID", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
    private UUID kioskId;
}