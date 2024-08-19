package com.example.pinokkio.api.category.dto.response;

import com.example.pinokkio.api.category.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Schema(description = "카테고리 응답 데이터")
public class CategoryResponse {
    @Schema(description = "카테고리 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private final UUID id;

    @Schema(description = "카테고리명", example = "Beverage")
    private final String name;

    public CategoryResponse(Category category) {
        this.id = category.getId();
        this.name = category.getName();
    }
}
