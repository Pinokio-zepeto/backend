package com.example.pinokkio.api.room.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@Schema(description = "화상 상담 입장 요청 DTO")
public class RoomEnterRequest {

    @Schema(description = "상담실 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID roomId;

    @Schema(description = "키오스크 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID kioskId;
}
