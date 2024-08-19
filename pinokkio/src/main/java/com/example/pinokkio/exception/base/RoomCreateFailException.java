package com.example.pinokkio.exception.domain.room;

import com.example.pinokkio.exception.base.NotFoundException;

import java.util.Map;

public class RoomCreateFailException extends NotFoundException {

    public RoomCreateFailException() {
        super(
                "ROOM_CREATE_FAILED_02",
                "방을 생성할 수 없습니다."
        );
    }

}
