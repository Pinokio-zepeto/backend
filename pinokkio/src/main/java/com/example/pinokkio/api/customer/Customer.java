//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.example.pinokkio.api.customer;

import com.example.pinokkio.api.pos.Pos;
import com.example.pinokkio.common.BaseEntity;
import com.example.pinokkio.common.type.Gender;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "customer")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Customer extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "BINARY(16)", name = "customer_id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pos_id")
    private Pos pos;

    @Column(nullable = false)
    private Gender gender;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private int age;

    @Column(nullable = true, columnDefinition = "VARBINARY(8000)")
    private byte[] faceEmbedding;

    /**
     * 실제 고객 생성자
     */
    @Builder
    public Customer(Pos pos, Gender gender, String phoneNumber, int age, byte[] faceEmbedding) {
        this.pos = pos;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
        this.age = age;
        this.faceEmbedding = faceEmbedding;
    }

    /**
     * 더미 고객 생성자 : FaceEmbed 정보가 없다
     * UUID = 00000000-0000-0000-0000-000000000000
     */
    @Builder
    public Customer(UUID id, Pos pos, Gender gender, String phoneNumber, int age) {
        this.id = new UUID(0L, 0L);
        this.pos = pos;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
        this.faceEmbedding = null;
        this.age = age;
    }

    public void updateFaceEmbedding(byte[] faceEmbedding) {
        this.faceEmbedding = faceEmbedding;
    }

    public void updatePos(Pos pos) {
        this.pos = pos;
    }
}
