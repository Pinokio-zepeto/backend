package com.example.pinokkio.api.kiosk.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "키오스크 정보 응답 DTO")
public class KioskResponse {

    @Schema(description = "키오스크 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private String kioskId;

    @Schema(description = "포스 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private String posId;

    @Schema(description = "더미 고객 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private String customerId;

    @Schema(description = "키오스크 email", example = "pos1@starbucks.com")
    private String email;

    public KioskResponse(String kioskId, String posId, String email, String customerId) {
        this.kioskId = kioskId;
        this.posId = posId;
        this.email = email;
        this.customerId = customerId;
    }

}
