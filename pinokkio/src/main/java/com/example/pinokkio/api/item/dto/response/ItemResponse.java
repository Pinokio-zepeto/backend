package com.example.pinokkio.api.item.dto.response;

import com.example.pinokkio.api.item.Item;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.UUID;

@Getter
@Schema(description = "아이템 응답 DTO")
public class ItemResponse {

    @Schema(description = "포스 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private final UUID posId;

    @Schema(description = "카테고리 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private final UUID categoryId;

    @Schema(description = "상품 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private final UUID itemId;

    @Schema(description = "아이템 가격", example = "10000")
    private final int price;

    @Schema(description = "아이템 수량", example = "10")
    private final int amount;

    @Schema(description = "아이템 이름", example = "빅맥")
    private final String name;

    @Schema(description = "아이템 세부 사항", example = "참깨빵 위에 순 쇠고기 패티 두 장 특별한 소스 양상추 치즈 피클 양파까지!")
    private final String detail;

    @Schema(description = "아이템 이미지 URL", example = "http://example.com/image.jpg")
    private final String file;

    @Schema(description = "스크린 표시 여부", example = "true")
    private final String isScreen;

    @Schema(description = "품절 여부", example = "false")
    private final String isSoldOut;

    public ItemResponse(Item item) {
        this.posId = item.getPos().getId();
        this.categoryId = item.getCategory().getId();
        this.itemId = item.getId();
        this.price = item.getPrice();
        this.amount = item.getAmount();
        this.name = item.getName();
        this.detail = item.getDetail();
        this.file = item.getItemImage();
        this.isScreen = item.getIsScreen().toString();
        this.isSoldOut = item.getIsSoldOut().toString();
    }
}
