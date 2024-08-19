//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.example.pinokkio.api.order.orderitem;

import com.example.pinokkio.api.item.Item;
import com.example.pinokkio.api.order.Order;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "order_item")
@Getter
@Setter(AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "BINARY(16)", name = "order_item_id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private UUID customerId;

    public OrderItem(Order order, Item item, UUID customerId, int quantity) {
        this.order = order;
        this.item = item;
        this.customerId = customerId;
        this.quantity = quantity;
    }

    public void updateOrder(Order order) {
        this.order = order;
    }
}
