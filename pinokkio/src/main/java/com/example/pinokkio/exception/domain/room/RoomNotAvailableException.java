package com.example.pinokkio.exception.domain.room;

import com.example.pinokkio.exception.base.BadInputException;
import com.example.pinokkio.exception.base.NotFoundException;

import java.util.Map;
import java.util.UUID;

/**
 * 400 BAD INPUT
 */
public class RoomNotAvailableException extends BadInputException {
    public RoomNotAvailableException(UUID roomId) {
        super(
                "BAD_INPUT_ROOM_01",
                "해당 방에 접근할 수 없습니다.",
                Map.of("roomId", String.valueOf(roomId))
        );
    }
}