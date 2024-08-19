package com.example.pinokkio.api.item;

import com.example.pinokkio.api.category.Category;
import com.example.pinokkio.api.pos.Pos;
import com.example.pinokkio.common.BaseEntity;
import com.example.pinokkio.common.type.IsScreen;
import com.example.pinokkio.common.type.IsSoldOut;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Item extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "BINARY(16)", name = "item_id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pos_id")
    private Pos pos;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private int amount;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String detail;

    @Column(nullable = true)
    private String itemImage;

    @Enumerated(EnumType.STRING)
    private IsScreen isScreen;

    @Enumerated(EnumType.STRING)
    private IsSoldOut isSoldOut;

    @Builder
    public Item(Pos pos, Category category, int price, int amount, String name, String detail, String itemImage) {
        this.isScreen = IsScreen.YES;
        this.isSoldOut = IsSoldOut.NO;
        this.pos = pos;
        this.category = category;
        this.price = price;
        this.amount = amount;
        this.name = name;
        this.detail = detail;
        this.itemImage = itemImage;
    }

    public void updateCategory(Category category) {
        this.category = category;
    }

    public void updatePrice(int price) {
        this.price = price;
    }

    public void updateAmount(int amount) {
        this.amount = amount;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateDetail(String detail) {
        this.detail = detail;
    }


    public void updateItemImage(String itemImage) {
        this.itemImage = itemImage;
    }

    public void updateIsScreen(IsScreen isScreen) {
        this.isScreen = isScreen;
    }

    public void updateIsSoldOut(IsSoldOut isSoldOut) {
        this.isSoldOut = isSoldOut;
    }


    public void toggleIsScreen() {
        if (this.isScreen.equals(IsScreen.NO)) {
            this.isScreen = IsScreen.YES;
        } else if (this.isScreen.equals(IsScreen.YES)) {
            this.isScreen = IsScreen.NO;
        }
    }

    public void toggleIsSoldOut() {
        if (this.isSoldOut.equals(IsSoldOut.NO)) {
            this.isSoldOut = IsSoldOut.YES;
        } else if (this.isSoldOut.equals(IsSoldOut.YES)) {
            this.isSoldOut = IsSoldOut.NO;
        }
    }

}
