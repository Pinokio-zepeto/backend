package com.example.pinokkio.api.order.dto.response;

import com.example.pinokkio.api.item.Item;
import com.example.pinokkio.api.order.orderitem.OrderItem;
import lombok.Getter;

import java.util.UUID;

@Getter
public class OrderItemDetail {
    private final UUID itemId;
    private final String itemName;
    private final int quantity;
    private final long price;

    public OrderItemDetail(OrderItem orderItem) {
        Item item = orderItem.getItem();
        this.itemId = item.getId();
        this.itemName = item.getName();
        this.quantity = orderItem.getQuantity();
        this.price = item.getPrice();
    }
}
