package com.example.pinokkio.api.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@Schema(description = "키오스크 회원가입 요청 DTO")
public class SignUpKioskRequest {

    @NotNull(message = "posId는 필수 값입니다.")
    @Schema(description = "POS ID", example = "UUID 타입", required = true)
    private UUID posId;
}