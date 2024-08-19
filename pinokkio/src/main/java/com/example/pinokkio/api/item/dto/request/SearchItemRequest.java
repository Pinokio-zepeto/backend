package com.example.pinokkio.api.item.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "아이템 검색 요청 DTO")
public class SearchItemRequest {

    @Schema(description = "검색 키워드", example = "아이스")
    private String keyWord;
}
