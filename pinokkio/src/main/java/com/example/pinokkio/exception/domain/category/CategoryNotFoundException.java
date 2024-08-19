package com.example.pinokkio.exception.domain.category;

import com.example.pinokkio.exception.base.NotFoundException;
import java.util.Map;
import java.util.UUID;

/**
 * 404 NOT FOUND
 */
public class CategoryNotFoundException extends NotFoundException {
    public CategoryNotFoundException(UUID categoryId) {
        super(
                "NOT_FOUND_CATEGORY_01",
                "아이디에 부합한 카테고리를 찾을 수 없습니다.",
                Map.of("categoryId", String.valueOf(categoryId))
        );
    }
}
