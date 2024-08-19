//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.example.pinokkio.api.order;

import com.example.pinokkio.api.customer.Customer;
import com.example.pinokkio.api.order.orderitem.OrderItem;
import com.example.pinokkio.api.pos.Pos;
import com.example.pinokkio.common.BaseEntity;
import com.example.pinokkio.common.type.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Order extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "BINARY(16)", name = "order_id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pos_id")
    private Pos pos;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    // 총 가격 필드 추가
    @Column(nullable = false)
    private long totalPrice;

    @Builder
    public Order(Pos pos, Customer customer, List<OrderItem> items, long totalPrice) {
        this.pos = pos;
        this.customer = customer;
        this.items = items;
        this.status = OrderStatus.ACTIVE;
        this.totalPrice = totalPrice;
    }

    public void toggleOrderStatus() {
        if(status.equals(OrderStatus.ACTIVE)) this.status = OrderStatus.CANCELLED;
        else this.status = OrderStatus.ACTIVE;
    }

    public void updateTotalPrice(long totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void initializeItems() {
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
    }


}
