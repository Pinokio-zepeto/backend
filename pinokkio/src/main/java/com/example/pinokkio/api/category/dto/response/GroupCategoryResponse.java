package com.example.pinokkio.api.category.dto.response;

import com.example.pinokkio.api.category.Category;
import com.example.pinokkio.common.response.GroupResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Schema(description = "그룹 카테고리 응답 DTO")
public class GroupCategoryResponse extends GroupResponse<Category, CategoryResponse> {
    public GroupCategoryResponse(List<Category> categoryList) {
        super(categoryList, CategoryResponse::new);
    }
}