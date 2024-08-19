package com.example.pinokkio.api.room.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Schema(description = "화상 상담 응답 DTO")
public class KioskRoomResponse {

    @Schema(description = "Room UUID", example = "123e4567-e89b-12d3-a456-426614174000")
    private String roomId;

    @Schema(description = "OpenVidu 비디오 공유를 위한 Access Token")
    private String videoToken;

    @Schema(description = "OpenVidu 화면 공유를 위한 Access Token")
    private String screenToken;
}
