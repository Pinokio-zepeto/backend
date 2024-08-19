package com.example.pinokkio.api.item.dto.response;

import com.example.pinokkio.api.item.Item;
import com.example.pinokkio.common.response.GroupResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import java.util.List;

@Getter
@Schema(description = "아이템 그룹 응답 DTO")
public class GroupItemResponse extends GroupResponse<Item, ItemResponse> {

    public GroupItemResponse(List<Item> itemList) {
        super(itemList, ItemResponse::new);
    }
}
