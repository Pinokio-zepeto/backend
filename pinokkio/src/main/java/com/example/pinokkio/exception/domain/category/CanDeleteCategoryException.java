package com.example.pinokkio.exception.domain.category;

import com.example.pinokkio.exception.base.BadInputException;

import java.util.Map;
import java.util.UUID;

public class CanDeleteCategoryException extends BadInputException {
    public CanDeleteCategoryException(UUID categoryId) {
        super(
                "BAD_INPUT_CATEGORY_01",
                "아이템이 존재하는 카테고리는 삭제할 수 없습니다..",
                Map.of("categoryId", String.valueOf(categoryId))
        );
    }
}