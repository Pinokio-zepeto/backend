package com.example.pinokkio.api.auth.dto.response;

import com.example.pinokkio.api.auth.AuthToken;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@AllArgsConstructor
@Schema(description = "이메일 중복여부 체크 응답 DTO")
public class EmailDuplicationResponse {
    @Schema(description = "이메일 중복 여부", example = "중복 시 true", required = true)
    private boolean isDuplicate;
}
