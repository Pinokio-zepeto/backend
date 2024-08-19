package com.example.pinokkio.api.category;

import com.example.pinokkio.api.pos.Pos;
import com.example.pinokkio.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "category")
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Category extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "BINARY(16)", name = "category_id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pos_id")
    private Pos pos;

    @Column(nullable = false)
    private String name;

    public void updateName(String name) {
        this.name = name;
    }
}