package com.example.pinokkio.exception.domain.room;

import com.example.pinokkio.exception.base.NotFoundException;

import java.util.Map;

public class RoomAccessRestrictedException extends NotFoundException {
    public RoomAccessRestrictedException() {
        super("ROOM_ACCESS_RESTRICTED_01", "현재 입장 가능한 방이 없습니다.");
    }

    public RoomAccessRestrictedException(String kioskId) {
        super(
                "ROOM_ACCESS_RESTRICTED_02",
                "이미 상담이 진행 중입니다.",
                Map.of("kioskId", String.valueOf(kioskId))
        );
    }

}
