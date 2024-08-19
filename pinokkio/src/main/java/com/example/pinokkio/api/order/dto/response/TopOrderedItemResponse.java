package com.example.pinokkio.api.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TopOrderedItemResponse {
    private UUID itemId;
    private String itemName;
    private int totalQuantity;
}
