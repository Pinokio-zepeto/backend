//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.example.pinokkio.api.kiosk;

import com.example.pinokkio.api.pos.Pos;
import com.example.pinokkio.common.BaseEntity;
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
@Table(name = "kiosk")
@Getter
@Setter(AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Kiosk extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "BINARY(16)", name = "kiosk_id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pos_id")
    private Pos pos;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Builder
    public Kiosk(Pos pos, String email, String password) {
        this.pos = pos;
        this.email = email;
        this.password = password;
    }

    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }

}
