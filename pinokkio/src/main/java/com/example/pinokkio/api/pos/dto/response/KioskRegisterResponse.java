package com.example.pinokkio.api.pos.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Schema(description = "키오스크 등록 응답 DTO")
public class KioskRegisterResponse {

    @Schema(description = "키오스크 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID kioskId;

    @Schema(description = "포스 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID posId;

    @Schema(description = "키오스크 email", example = "kiosk1213@starbucks.com")
    private String email;

    @Schema(description = "키오스크 비밀번호", example = "1234")
    private String password;

    public KioskRegisterResponse(UUID kioskId, UUID posId, String email, String password) {
        this.kioskId = kioskId;
        this.posId = posId;
        this.email = email;
        this.password = password;
    }

}
