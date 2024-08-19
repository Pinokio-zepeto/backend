package com.example.pinokkio.exception.domain.item;

import com.example.pinokkio.exception.base.BadInputException;

import java.util.Map;
import java.util.UUID;

public class ItemAmountException extends BadInputException {
    public ItemAmountException(UUID itemId) {
        super(
                "BAD_INPUT_ITEM_01",
                "아이템의 수량이 부족합니다.",
                Map.of("itemId", String.valueOf(itemId))
        );
    }
}
