package com.example.pinokkio.exception.domain.room;

import com.example.pinokkio.exception.base.NotFoundException;

import java.util.Map;
import java.util.UUID;

/**
 * 404 NOT FOUND
 */
public class RoomNotFoundException extends NotFoundException {
    public RoomNotFoundException(UUID roomId) {
        super(
                "NOT_FOUND_ROOM_01",
                "아이디에 부합한 방을 찾을 수 없습니다.",
                Map.of("roomId", String.valueOf(roomId))
        );
    }

    public RoomNotFoundException(String message) {
        super(
                "NOT_FOUND_ROOM_02",
                message
        );
    }
}