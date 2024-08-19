package com.example.pinokkio.exception.domain.item;

import com.example.pinokkio.exception.base.NotFoundException;
import java.util.Map;
import java.util.UUID;

/**
 * 404 NOT FOUND
 */
public class ItemNotFoundException extends NotFoundException {
    public ItemNotFoundException(UUID itemId) {
        super(
                "NOT_FOUND_ITEM_01",
                "아이디에 부합한 상품을 찾을 수 없습니다.",
                Map.of("itemId", String.valueOf(itemId))
        );
    }
}