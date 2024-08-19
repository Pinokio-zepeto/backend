package com.example.pinokkio.api.category.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@Schema(description = "카테고리 생성 요청 DTO")
public class CategoryRequest {
    @Schema(description = "카테고리명", example = "Beverage")
    private final String name;
}
